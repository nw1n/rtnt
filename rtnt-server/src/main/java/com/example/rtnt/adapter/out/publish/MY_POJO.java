package com.example.rtnt.adapter.out.publish;

public class MY_POJO {
    private String message;

    public MY_POJO() {
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    // Convenience setter to match the existing demo call site.
    public void set(String message) {
        this.setMessage(message);
    }

    @Override
    public String toString() {
        return "MY_POJO{message='" + this.message + "'}";
    }
}
