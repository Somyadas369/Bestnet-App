# Connected to production

This app came out of Google AI Studio as a complete UI running entirely on a
local Room database — Retrofit and OkHttp were declared as dependencies but
nothing used them. This document covers what was added to make it talk to the
live BestNet API, and — just as important — what is still local mock data.

**API base:** `https://crm.bestnet.in/api/v1/` (HTTPS only;
`usesCleartextTraffic="false"`)

## What was added

| File | Purpose |
|---|---|
| `data/remote/ApiModels.kt` | Wire DTOs. Every optional server field is nullable — `fullName` really is null until set, and non-null types would crash on valid responses |
| `data/remote/BestNetApi.kt` | Retrofit interface |
| `data/remote/TokenStore.kt` | Tokens in `EncryptedSharedPreferences` |
| `data/remote/ApiClient.kt` | OkHttp, auth header, refresh-on-401 |
| `data/remote/ApiMappers.kt` | Server shapes → the app's Room entities |
| `data/repository/SessionRepository.kt` | Login, logout, sync |

Three fixes were needed before any of it could work:

1. **No `INTERNET` permission in the manifest.** Every call would have failed at
   runtime with a `SecurityException`. This is the kind of thing that looks like
   a broken API rather than a missing line of XML.
2. **`_isLoggedIn` started as `true`** — the app skipped login entirely.
3. **No `androidx.security:security-crypto` dependency**, so there was nothing
   to store tokens in securely.

## Design: the server syncs *into* Room

The UI keeps reading the same Room `Flow`s it always did; `SessionRepository`
pulls server data down and writes it there. Two reasons: the screens didn't have
to be rewritten, and the app still renders the last known state with no network,
which matters on a phone.

Sync **replaces** the residents and notices tables rather than upserting. Room
ids are autoGenerate `Long`s and server ids are UUID strings, so there is no
stable key to match on — and replacing means a home the resident no longer
belongs to actually disappears.

## Auth flow

`requestOtp` → the code is delivered over **WhatsApp** (ChatNPay), not SMS →
`verifyOtp` → tokens stored → **data synced before the app opens**.

That order is deliberate: the old `login()` just flipped a boolean, which meant
the UI could show a signed-in shell containing nothing. Now, if sync fails the
session is cleared and the error is shown, rather than stranding the user in a
half-loaded app that looks functional.

Refresh-on-401 is handled by an OkHttp `Authenticator` with a lock: when several
requests 401 at once only the first refreshes, and the rest pick up its token.
Without that, each would burn the single-use rotating refresh token and the last
one would log the user out. `responseCount` guards against looping if the fresh
token is also rejected.

## Real vs mock, per screen

| Screen | Real | Still local mock |
|---|---|---|
| Login | Whole flow — OTP over WhatsApp, tokens, session | — |
| Home | Greeting, unit, community (`/users/me`, `/me/units`) | Internet status card, promo banner |
| Notifications | `GET /notifications`, categories, timestamps | — |
| Profile | Name, phone, unit, switch home, logout | Edit profile, family, vehicles, etc. |
| Intercom | `GET /me/intercom` available via `SessionRepository.myIntercom()` | Directory, neighbours, and placing a call |
| Raise Complaint | **Real** — files a ticket staff actually receive | Photo attachment; the status/priority controls on existing rows |
| Visitors | **Real** — visit log and pre-approval | QR / passcode gate entry (doesn't exist in the product) |
| My Services | — | Everything |
| Community | — | Everything except Notices |

### Raising a complaint — now real

It used to invent a ticket number (`"#CMP-2026-" + (100..999).random()`), store
it in Room, and show a success dialog. Nothing left the phone. Worse, the screen
set `currentStep = 3` *before* the callback resolved, so the confirmation
appeared regardless of outcome.

It now calls `POST /units/:unitId/tickets` — a genuine self-service endpoint
authorised by the caller's active `UnitMembership`, which already existed in the
backend (an earlier note here claiming otherwise was wrong). The confirmation
dialog only appears once the server returns a ticket, the reference shown is
derived from the server's own id, and a failure is displayed on the form.

Two things this needed:

- **The route renders `MaintenanceComplaintsScreen`, not `RaiseComplaintScreen`.**
  The latter is imported in `MainActivity` but never used — dead code. Both were
  updated, but only the former is reachable.
- **Category names differ between app and server.** The UI tile says "Plumber",
  the tenant's category is "Plumbing". `matchCategory()` resolves this with an
  alias table and returns null on no match, so an unrecognised category tells the
  resident their community hasn't set it up rather than silently filing under the
  wrong team and SLA. The five missing categories (Electrician, Housekeeping,
  Internet Issue, General Maintenance, Others) were created in the tenant.

Verified against production end to end: resident logs in → lists categories →
raises a ticket (`201`, status `NEW`, SLA computed) → sees it in `GET /my/tickets`
→ **and staff see it in the community queue**. The loop closes.

### Visitors — now real

Unlike tickets, this genuinely needed new backend work. Three self-service
endpoints were added (`visitor-visits.controller.ts`), all authorised by the
caller's own `UnitMembership` rather than a staff permission:

- `POST /me/visitor-visits` — pre-approve an expected visitor
- `GET /me/visitor-visits` — the visit log for every home the resident belongs to
- `POST /me/visitor-visits/:id/cancel` — cancel one's own pre-approval

Deliberate constraints, each of which is enforced and tested:

- A resident can only ever create a **PRE_APPROVED** visit. "I expect this
  person" is a different claim from a guard's "this person is at the gate now";
  allowing WALK_IN would let a resident inject arrivals into the gate log.
- `gateLabel` is `"Pending gate"`, not a guessed "Main Gate" — the resident
  cannot know which gate will be used, and the guard sets it on arrival.
- Cancelling is restricted to visits still in `PRE_APPROVED`. Once a guard marks
  someone ARRIVED, the record describes something that physically happened and
  is not the resident's to erase.
- `scheduledAt` must be in the future (5-minute skew tolerance): a pre-approval
  in the past can never be acted on, so it's a mistake, not a record.

**No pass code is issued anywhere.** The old local implementation generated
`(100000..999999).random()` and told the resident it was a gate pass. The
product has no such concept, so the dialog now says what actually happens —
the visitor appears on the guard's expected-arrivals list — and the button says
"Pre-approve" rather than "Generate Pass".

Two app bugs found while wiring this:

- **The pre-approve dialog could never open from the Visitors screen.** It was
  rendered inside `MainShellScreen`, but `MAIN_SHELL` and `VISITORS` are sibling
  NavHost destinations — so tapping the FAB set a flag that nothing was
  composing. The dialog is now hoisted above the NavHost.
- `visitorStatusLabel` maps the server's 14-state machine to short labels and
  **passes unknown statuses through unchanged**, because showing a denied or
  unrecognised visit as "Approved" would be the worst possible failure here.

Verified against production: pre-approve (`201`) → **staff see it in the
community queue** → resident cancels (`CANCELLED`) → second cancel refused
(`400`). Isolation checked too: another resident's unit is `403`, a past date is
`400`.

### Services and Community — partly real

Both needed new backend models (`prisma/schema.prisma`, migration
`20260906063500_add_subscriptions_events_contacts`) and a new
`community-life` module with three resident endpoints — `GET /me/subscriptions`,
`GET /me/events`, `GET /me/emergency-contacts` — plus staff routes to manage
them.

`ServicePlan` already existed as a tenant catalogue, but nothing linked a unit
to a plan; `Subscription` is that link. It is **deliberately not a billing
system**: no invoices, payments, auto-pay or usage. Taking money needs a payment
provider and a decision about who holds the mandate, which is not something to
improvise. `currentPeriodEnd` is simply the "valid till" date staff set.

| Screen row | State |
|---|---|
| Services — active plan card | **Real** (plan name, description, status, valid-till) |
| Services — billing, usage, Wi-Fi settings, upgrade | Mock |
| Community — Notices | **Real** |
| Community — Events | **Real** |
| Community — Emergency Contacts | **Real** |
| Community — Amenities Booking | Mock, **now labelled as such** |

**Amenities booking is still fake and now says so.** It previously listed
"Tennis Court: Booked until 5 PM" as though that were live availability. There
is no amenity or booking model at all, so a resident could believe they had
reserved something. It now carries an explicit line saying booking isn't built
and the list is an example.

**The resident directory was deliberately not built.** Publishing neighbours'
names and phone numbers to every other resident is a privacy decision with
consent implications, not a missing CRUD screen. It needs an explicit opt-in
model before it should exist.

A validation bug was caught by testing: the emergency-contact DTO required a
4-character minimum phone, which rejected India's three-digit emergency short
codes (100, 101, 108, 112) — exactly the numbers the table exists to hold.

### Migration hazard worth knowing

`prisma migrate diff` wanted to `DROP TABLE ps_aors, ps_auths, ps_contacts,
ps_endpoints, ps_transports` — the Asterisk PJSIP realtime tables. They are
created by a raw-SQL migration and are not modelled in `schema.prisma`, so
Prisma reads them as drift. Applying that generated migration unedited would
take the intercom down. The committed migration is hand-trimmed, and any
regeneration must be trimmed the same way.

## Verified

- `./gradlew :app:assembleDebug` — clean
- `./gradlew :app:testDebugUnitTest` — **21 tests, 0 failures**, covering unit
  label formatting, null names/communities, notification category mapping,
  relative timestamps, E.164 conversion, and ticket-category matching
  (including the Plumber→Plumbing alias and refusing to guess on no match)
- The complaint flow exercised live against production, resident *and* staff side
- `https://crm.bestnet.in/api/v1/` confirmed present in the compiled dex
- `android.permission.INTERNET` confirmed in the built manifest

**Not verified: the app has never been run on a device or emulator.** None was
available. So while every API endpoint it calls has been exercised directly
against production and the pure logic is unit-tested, the actual login journey
on a handset — Compose state, navigation, keyboard behaviour, Moshi parsing of
live responses — has not been observed once. Expect first-run bugs there.

## Beta-readiness pass

Four bugs found and fixed, none of which a build or the unit tests would have
caught:

1. **`java.time` crash on Android 7.** `minSdk` is 24 but `Instant`,
   `ZoneId` and `DateTimeFormatter` are API 26+. The app would install fine on
   Android 7/7.1 and crash the first time it rendered a timestamp — every
   notification, visit, ticket and event. Found by `lintDebug`, which is why
   lint is worth running even when everything compiles. Fixed with core library
   desugaring (`isCoreLibraryDesugaringEnabled`), keeping API 24 support;
   verified by finding `j$/time/Instant` in the shipped dex.
2. **Real tickets were never displayed.** `myTickets()` existed in the
   repository but nothing called it. Submitting filed a real ticket on the
   server while the list on screen still read Room, so a resident saw seeded
   sample complaints and never their own. Now synced on login and after every
   submission, with category names resolved.
3. **Seeded fake data.** Room populated residents, complaints, visitors,
   notices and community notices on first launch — including a fake identity
   ("Rahul Sharma", A-1201, Sunrise Apartments). Every one of those tables now
   has a real server source, so the sample rows only ever appeared *alongside*
   real data. Seeding removed entirely.
4. **Logout leaked between accounts.** It cleared residents and notices but not
   visitors or complaints, and none of the in-memory lists (plan, events,
   emergency contacts). On a shared test handset the next person to sign in saw
   the previous resident's data until a sync happened to replace it. All of it
   is cleared now.

Also fixed: three `DefaultLocale` warnings (number and clock formatting now
pinned to `Locale.US` so a device locale can't substitute digits or separators).

Lint is now **0 errors**. Remaining warnings are dependency-currency notices,
unused resources and icon-density nits — none behavioural.

## Before release

- `applicationId` is `com.aistudio.bestnet.app`, which **is** acceptable to
  Play — Play checks the applicationId, not the Kotlin package. An earlier note
  here claiming Play would reject this was wrong. The `namespace` is still
  `com.example`, which is untidy and worth renaming for readability, but it is
  not a release blocker.
- The release signing config expects `KEYSTORE_PATH`, `STORE_PASSWORD` and
  `KEY_PASSWORD` in the environment, and an upload keystore that doesn't exist
  yet. `debug.keystore` was generated locally and is gitignored.
- `google-services.json` is absent. The Google Services plugin is set to
  `MissingGoogleServicesStrategy.WARN`, so the build passes, but any Firebase
  feature will fail at runtime.
