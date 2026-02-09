package com.example.dailyreport.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "request_file")
public class RequestFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_item_id", nullable = false)
    private RequestItem requestItem;

    @Column(nullable = false)
    private String originalName;

    @Column(nullable = false)
    private String storedName;

    @Column(nullable = false)
    private long size;

    @Column(nullable = false)
    private String mimeType;

    public Long getId() {
        return id;
    }

    public RequestItem getRequestItem() {
        return requestItem;
    }

    public void setRequestItem(RequestItem requestItem) {
        this.requestItem = requestItem;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getStoredName() {
        return storedName;
    }

    public void setStoredName(String storedName) {
        this.storedName = storedName;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
