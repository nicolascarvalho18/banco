package com.bancosap.dto.response;

import com.bancosap.enums.RoleName;
import com.bancosap.enums.UserStatus;
import java.time.LocalDate;

public class UserSummaryResponse {
    private Long id;
    private String fullName;
    private String username;
    private String cpfMasked;
    private LocalDate birthDate;
    private String phone;
    private String email;
    private RoleName role;
    private UserStatus status;
    private String profilePhotoUrl;
    private String address;
    private boolean hasPin;
    private boolean twoFactorEnabled;
    private String themePreference;

    public UserSummaryResponse() {}

    public UserSummaryResponse(Long id, String fullName, String cpfMasked, LocalDate birthDate,
                               String phone, String email, RoleName role, UserStatus status,
                               String profilePhotoUrl, String address, boolean hasPin) {
        this(id, fullName, null, cpfMasked, birthDate, phone, email, role, status, profilePhotoUrl, address, hasPin, false, "dark");
    }

    public UserSummaryResponse(Long id, String fullName, String username, String cpfMasked, LocalDate birthDate,
                               String phone, String email, RoleName role, UserStatus status,
                               String profilePhotoUrl, String address, boolean hasPin,
                               boolean twoFactorEnabled, String themePreference) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.cpfMasked = cpfMasked;
        this.birthDate = birthDate;
        this.phone = phone;
        this.email = email;
        this.role = role;
        this.status = status;
        this.profilePhotoUrl = profilePhotoUrl;
        this.address = address;
        this.hasPin = hasPin;
        this.twoFactorEnabled = twoFactorEnabled;
        this.themePreference = themePreference;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getCpfMasked() { return cpfMasked; }
    public void setCpfMasked(String cpfMasked) { this.cpfMasked = cpfMasked; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public RoleName getRole() { return role; }
    public void setRole(RoleName role) { this.role = role; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    public String getProfilePhotoUrl() { return profilePhotoUrl; }
    public void setProfilePhotoUrl(String profilePhotoUrl) { this.profilePhotoUrl = profilePhotoUrl; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public boolean isHasPin() { return hasPin; }
    public void setHasPin(boolean hasPin) { this.hasPin = hasPin; }

    public boolean isTwoFactorEnabled() { return twoFactorEnabled; }
    public void setTwoFactorEnabled(boolean twoFactorEnabled) { this.twoFactorEnabled = twoFactorEnabled; }

    public String getThemePreference() { return themePreference; }
    public void setThemePreference(String themePreference) { this.themePreference = themePreference; }
}
