package ro.unibuc.prodeng.util;

import ro.unibuc.prodeng.model.UserRole;

import java.util.Set;

public class CategoryPermissions {
    private static final Set<String> CONTENT_CREATOR_CATEGORIES = Set.of(
        "animals",
        "nature",
        "travel",
        "architecture",
        "street",
        "portrait"
    );

    private static final Set<String> VIEWER_CATEGORIES = Set.of();

    /**
     * Check if a user with the given role can upload to the specified category
     */
    public static boolean canUploadToCategory(UserRole role, String category) {
        if (role == null || category == null) {
            return false;
        }
        
        return switch (role) {
            case CONTENT_CREATOR -> CONTENT_CREATOR_CATEGORIES.contains(category.toLowerCase());
            case VIEWER -> false;
        };
    }

    /**
     * Get all categories that a role can upload to
     */
    public static Set<String> getUploadableCategories(UserRole role) {
        return switch (role) {
            case CONTENT_CREATOR -> Set.copyOf(CONTENT_CREATOR_CATEGORIES);
            case VIEWER -> Set.copyOf(VIEWER_CATEGORIES);
        };
    }
}
