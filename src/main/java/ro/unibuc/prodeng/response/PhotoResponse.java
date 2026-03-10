package ro.unibuc.prodeng.response;

public record PhotoResponse(
    String id,
    String title,
    String description,
    String location,
    String URL,
    int likes,
    String category,
    String uploadedBydUserId
) {}
