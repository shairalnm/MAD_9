package com.example.inclass10;

public class NotesData {

    String userId, id, textNote;

    public NotesData(){}

    public NotesData(String userId, String id, String textNote) {
        this.userId = userId;
        this.id = id;
        this.textNote = textNote;
    }

    @Override
    public String toString() {
        return "NotesData{" +
                "userId='" + userId + '\'' +
                ", id='" + id + '\'' +
                ", textNote='" + textNote + '\'' +
                '}';
    }
}
