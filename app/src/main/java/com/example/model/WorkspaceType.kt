package com.example.model

/**
 * Supported financial workspace types for FINITYARC.
 * Enables clean separation between personal household finance and business operational cash flow.
 */
enum class WorkspaceType(val displayName: String) {
    PERSONAL("Personal"),
    BUSINESS("Business")
}
