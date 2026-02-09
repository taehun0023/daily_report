package com.example.dailyreport.service;

import com.example.dailyreport.domain.RequestFile;
import com.example.dailyreport.domain.RequestItem;
import com.example.dailyreport.domain.RequestStatus;
import com.example.dailyreport.repository.RequestItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RequestService {
    private final RequestItemRepository requestRepository;
    private final FileStorageService fileStorageService;

    public RequestService(RequestItemRepository requestRepository, FileStorageService fileStorageService) {
        this.requestRepository = requestRepository;
        this.fileStorageService = fileStorageService;
    }

    public List<RequestItem> findAll() {
        return requestRepository.findAll();
    }

    public RequestItem findById(Long id) {
        return requestRepository.findById(id).orElseThrow();
    }

    @Transactional
    public RequestItem create(RequestItem item, List<MultipartFile> files) throws IOException {
        if (files != null) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    RequestFile requestFile = fileStorageService.store(file);
                    item.addFile(requestFile);
                }
            }
        }
        return requestRepository.save(item);
    }

    @Transactional
    public RequestItem update(Long id, RequestItem updates) {
        RequestItem item = findById(id);
        item.setTitle(updates.getTitle());
        item.setContent(updates.getContent());
        item.setUserEmail(updates.getUserEmail());
        return item;
    }

    @Transactional
    public void delete(Long id) {
        requestRepository.deleteById(id);
    }

    @Transactional
    public RequestItem updateStatus(Long id, RequestStatus status) {
        RequestItem item = findById(id);
        item.setStatus(status);
        if (status == RequestStatus.PROCESSED) {
            item.setProcessedAt(LocalDateTime.now());
        } else {
            item.setProcessedAt(null);
        }
        return item;
    }
}
