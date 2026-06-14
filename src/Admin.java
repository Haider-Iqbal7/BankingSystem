public class Admin {
    String adminId;
    String userName;
    String password;
    String fullName;
    AdminRole adminRole;
    boolean isActive;

    public enum AdminRole {
        Super_Admin, Manager, Teller, Loan_Officer
    }

    public Admin(String adminId, String userName,
                 String password, String fullName, AdminRole adminRole) {
        this.adminId = adminId;
        this.userName = userName;
        this.password = password;
        this.fullName = fullName;
        this.adminRole = adminRole;
        this.isActive = true;
    }

    public String getAdminId() {
        return adminId;
    }

    public String getUsername() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getFullName() {
        return fullName;
    }

    public AdminRole getRole() {
        return adminRole;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return String.format("Admin: %-10s | Name: %-20s | Role: %-15s | Status: %s",
                userName, fullName, adminRole, isActive ? "ACTIVE" : "INACTIVE");
    }
}