package vn.iotstar.exceptions;
public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException() { super("Email đã được đăng ký."); }
}
