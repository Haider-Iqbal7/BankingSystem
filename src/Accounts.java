import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Accounts implements Comparable<Accounts>{
String Account_Number;
String Owner_name;
String cnic;
String Phone_no;
String address;
AccountType accountType;
double balance;
String pin;
boolean isActive;
LocalDateTime createdAt;;

    public enum AccountType {
        SAVINGS, CURRENT, FIXED_DEPOSIT
    }
    public Accounts(String Account_Number, String Owner_name, String cnic,
                   String phone_no, String address, AccountType accountType,
                   double initialDeposit, String pin) {
        this.Account_Number = Account_Number;
        this.Owner_name = Owner_name;
        this.cnic = cnic;
        this.Phone_no = Phone_no;
        this.address = address;
        this.accountType = accountType;
        this.balance = initialDeposit;
        this.pin = pin;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
    }
    String getAccountNumber() {
        return Account_Number;
    }
     String getOwnerName() {
        return Owner_name;
    }
    String getCnic() {
        return cnic;
    }
     String getPhone_no() {
        return Phone_no;
    }
     String getAddress() {
        return address;
    }
     AccountType getAccountType() {
        return accountType;
    }
    double getBalance() {
        return balance;
    }
     String getPin() {
        return pin;
    }
     boolean isActive() {
        return isActive;
    }
     LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Setters
    public void setOwnerName(String OwnerName) {
        this.Owner_name= OwnerName;
    }
    public void setPhone_no(String phone_no) {
        this.Phone_no = phone_no;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public void setBalance(double balance) {
        this.balance = balance;
    }
    public void setPin(String pin) {
        this.pin = pin;
    }
    public void setActive(boolean active) {
        isActive = active;
    }

    public void deposit(double amount) {
        this.balance += amount;
    }
    public boolean withdraw(double amount) {
        if (amount <= balance) {
            this.balance -= amount;
            return true;
        }
        return false;
    }

    @Override
    public int compareTo(Accounts other) {
        return this.Account_Number.compareTo(other.Account_Number);
    }
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        return String.format(
                "Account No: %s | Owner: %-20s | Type: %-15s | Balance: PKR %.2f | Status: %s | Created: %s",
                Account_Number, Owner_name, accountType, balance,
                isActive ? "ACTIVE" : "INACTIVE", createdAt.format(fmt));
    }
}

