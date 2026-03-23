package ro.unibuc.prodeng.model;

public enum UserRole {
    CONTENT_CREATOR("Content Creator - can upload photos"),
    VIEWER("Viewer - can only view photos");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
