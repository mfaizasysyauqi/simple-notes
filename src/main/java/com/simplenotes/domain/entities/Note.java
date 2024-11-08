// Note.java
package com.simplenotes.domain.entities;

public class Note {
    private Long id;
    private String title;
    private String content;
    private Long categoryId;
    private String createdAt;
    private String updatedAt;

    // Constructor with all fields except timestamps
    public Note(Long id, String title, String content, Long categoryId) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.categoryId = categoryId;
    }

    // Constructor with all fields
    public Note(Long id, String title, String content, Long categoryId, 
                String createdAt, String updatedAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.categoryId = categoryId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Long getId() { 
        return id; 
    }

    public void setId(Long id) { 
        this.id = id; 
    }

    public String getTitle() { 
        return title; 
    }

    public void setTitle(String title) { 
        this.title = title; 
    }

    public String getContent() { 
        return content; 
    }

    public void setContent(String content) { 
        this.content = content; 
    }

    public Long getCategoryId() { 
        return categoryId; 
    }

    public void setCategoryId(Long categoryId) { 
        this.categoryId = categoryId; 
    }

    public String getCreatedAt() { 
        return createdAt; 
    }

    public void setCreatedAt(String createdAt) { 
        this.createdAt = createdAt; 
    }

    public String getUpdatedAt() { 
        return updatedAt; 
    }

    public void setUpdatedAt(String updatedAt) { 
        this.updatedAt = updatedAt; 
    }

    @Override
    public String toString() {
        return "Note{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", categoryId=" + categoryId +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Note note = (Note) o;
        return id != null ? id.equals(note.id) : note.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
