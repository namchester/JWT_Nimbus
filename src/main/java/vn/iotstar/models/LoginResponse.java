package vn.iotstar.models;
// expiresIn tinh bang MILLISECOND, giu dung contract cua bai giang.
public record LoginResponse(String token, long expiresIn) { }
