package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "residents")
data class Resident(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val name: String,
  val phone: String,
  val unit: String,
  val communityName: String,
  val isCurrent: Boolean = false
)

@Entity(tableName = "complaints")
data class Complaint(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val ticketNumber: String,
  val category: String,
  val description: String,
  val status: String = "Submitted", // Submitted, In Progress, Resolved
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "visitors")
data class Visitor(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val name: String,
  val type: String, // Delivery, Guest, Service, Cab
  val unit: String,
  val timestampText: String,
  val status: String = "Approved", // Approved, Pending, Denied
  val isPreApproved: Boolean = false,
  val passCode: String? = null
)

@Entity(tableName = "notices")
data class Notice(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String,
  val body: String,
  val category: String, // Announcements, Services, Community
  val timestampText: String,
  val isRead: Boolean = false,
  val iconType: String // water, maintenance, event, complaint, visitor
)

data class IntercomContact(
  val id: String,
  val name: String,
  val role: String,
  val extension: String,
  val isStaff: Boolean,
  val unit: String? = null
)
