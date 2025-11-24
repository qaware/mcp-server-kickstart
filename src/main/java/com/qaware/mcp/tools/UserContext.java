package com.qaware.mcp.tools;

import java.util.List;

public record UserContext(List<String> activeContracts, List<String> recentlyBookedProjects, List<String> recentlyBookedAccounts) {
}
