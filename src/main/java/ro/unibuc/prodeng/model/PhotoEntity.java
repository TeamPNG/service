package ro.unibuc.prodeng.model;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "photos")
public record PhotoEntity(
    @Id String id,
    String title,
    String description,
    String location,
    String URL,
    int likes,
    String category,
    String uploadedBydUserId
) {
    public PhotoEntity( String title, String description, String location, String URL, int likes, String category, String uploadedBydUserId){
        this(null, title,  description, location, URL, likes, category, uploadedBydUserId);
    }

    public PhotoEntity( String title, String description, String location, String URL, String category, String uploadedBydUserId){
        this(null, title,  description, location, URL, 0, category, uploadedBydUserId);
    }
}
