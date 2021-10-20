package main.api.response;

public class AuthResponse {

    private boolean result;
    private User user;



    public AuthResponse(boolean result, User user) {
        this.result = result;
        this.user = user;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }



    private class User {
        private int id;
        private String name;
        private String photo;
        private String email;
        private boolean moderation;
        private int moderationCount;
        private boolean settings;

        public User(int id, String name, String photo, String email, boolean moderation, int moderationCount, boolean settings) {
            this.id = id;
            this.name = name;
            this.photo = photo;
            this.email = email;
            this.moderation = moderation;
            this.moderationCount = moderationCount;
            this.settings = settings;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPhoto() {
            return photo;
        }

        public void setPhoto(String photo) {
            this.photo = photo;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public boolean isModeration() {
            return moderation;
        }

        public void setModeration(boolean moderation) {
            this.moderation = moderation;
        }

        public int getModerationCount() {
            return moderationCount;
        }

        public void setModerationCount(int moderationCount) {
            this.moderationCount = moderationCount;
        }

        public boolean isSettings() {
            return settings;
        }

        public void setSettings(boolean settings) {
            this.settings = settings;
        }
    }

}
