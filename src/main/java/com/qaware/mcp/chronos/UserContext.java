package com.qaware.mcp.chronos;

import java.util.List;

/**
 * Context information for a Chronos user.
 * Contains the user's active contracts and recently used projects and accounts
 * to help with booking suggestions and validation.
 *
 * @param activeContracts List of active contract numbers for the user
 * @param recentlyBookedProjects List of project names the user has recently booked time to
 * @param recentlyBookedAccounts List of account names the user has recently used
 */
public record UserContext(List<String> activeContracts, List<String> recentlyBookedProjects, List<String> recentlyBookedAccounts) {
}
