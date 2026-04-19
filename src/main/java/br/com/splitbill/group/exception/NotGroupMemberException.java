package br.com.splitbill.group.exception;

public class NotGroupMemberException extends RuntimeException {
    public NotGroupMemberException(String message) {
        super(message);
    }
}
