package com.example.dailyreport.web;

import com.example.dailyreport.domain.RequestFile;
import com.example.dailyreport.domain.RequestItem;
import com.example.dailyreport.domain.RequestStatus;
import com.example.dailyreport.repository.RequestFileRepository;
import com.example.dailyreport.service.FileStorageService;
import com.example.dailyreport.service.RequestService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.util.List;

@Controller
@RequestMapping("/requests")
public class RequestController {
    private final RequestService requestService;
    private final RequestFileRepository fileRepository;
    private final FileStorageService fileStorageService;

    public RequestController(RequestService requestService,
                             RequestFileRepository fileRepository,
                             FileStorageService fileStorageService) {
        this.requestService = requestService;
        this.fileRepository = fileRepository;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("requests", requestService.findAll());
        return "requests/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("requestItem", new RequestItem());
        return "requests/form";
    }

    @PostMapping
    public String create(@ModelAttribute RequestItem requestItem,
                         @RequestParam(value = "files", required = false) List<MultipartFile> files) throws Exception {
        requestService.create(requestItem, files);
        return "redirect:/requests";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("requestItem", requestService.findById(id));
        model.addAttribute("statuses", RequestStatus.values());
        return "requests/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("requestItem", requestService.findById(id));
        return "requests/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @ModelAttribute RequestItem requestItem) {
        requestService.update(id, requestItem);
        return "redirect:/requests/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        requestService.delete(id);
        return "redirect:/requests";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam RequestStatus status) {
        requestService.updateStatus(id, status);
        return "redirect:/requests/" + id;
    }

    @PostMapping("/{id}/files")
    public String uploadFiles(@PathVariable Long id,
                              @RequestParam(value = "files", required = false) List<MultipartFile> files) throws Exception {
        RequestItem item = requestService.findById(id);
        requestService.create(item, files);
        return "redirect:/requests/" + id;
    }

    @GetMapping("/files/{fileId}")
    public ResponseEntity<Resource> download(@PathVariable Long fileId) throws MalformedURLException {
        RequestFile file = fileRepository.findById(fileId).orElseThrow();
        Resource resource = fileStorageService.loadAsResource(file.getStoredName());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getOriginalName() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, file.getMimeType())
                .body(resource);
    }
}
