package com.example.dailyreport.web;

import com.example.dailyreport.domain.RequestFile;
import com.example.dailyreport.domain.SupportTicket;
import com.example.dailyreport.domain.TicketStatus;
import com.example.dailyreport.repository.RequestFileRepository;
import com.example.dailyreport.repository.UserAccountRepository;
import com.example.dailyreport.service.FileStorageService;
import com.example.dailyreport.service.SupportTicketService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/supports")
public class SupportTicketController {
    private final SupportTicketService ticketService;
    private final RequestFileRepository fileRepository;
    private final FileStorageService fileStorageService;
    private final UserAccountRepository userRepository;

    public SupportTicketController(SupportTicketService ticketService,
                             RequestFileRepository fileRepository,
                             FileStorageService fileStorageService,
                             UserAccountRepository userRepository) {
        this.ticketService = ticketService;
        this.fileRepository = fileRepository;
        this.fileStorageService = fileStorageService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String list(@RequestParam(required = false) TicketStatus status,
                       @RequestParam(required = false) Long userId,
                       @RequestParam(required = false) String inquiryType,
                       @RequestParam(required = false) String category,
                       @RequestParam(required = false) LocalDate fromDate,
                       @RequestParam(required = false) LocalDate toDate,
                       Model model) {
        model.addAttribute("tickets", ticketService.findFiltered(status, userId, inquiryType, category, fromDate, toDate));
        model.addAttribute("statuses", TicketStatus.values());
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("status", status);
        model.addAttribute("userId", userId);
        model.addAttribute("inquiryType", inquiryType);
        model.addAttribute("category", category);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        return "supports/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("ticket", new SupportTicket());
        model.addAttribute("users", userRepository.findAll());
        return "supports/form";
    }

    @PostMapping
    public String create(@ModelAttribute SupportTicket ticket,
                         @RequestParam(value = "files", required = false) List<MultipartFile> files) throws Exception {
        ticketService.create(ticket, files);
        return "redirect:/supports";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("ticket", ticketService.findById(id));
        model.addAttribute("statuses", TicketStatus.values());
        return "supports/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("ticket", ticketService.findById(id));
        model.addAttribute("users", userRepository.findAll());
        return "supports/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @ModelAttribute SupportTicket ticket) {
        ticketService.update(id, ticket);
        return "redirect:/supports/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        ticketService.delete(id);
        return "redirect:/supports";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam TicketStatus status,
                               @RequestParam(required = false, defaultValue = "list") String redirectTo) {
        ticketService.updateStatus(id, status);
        if ("list".equalsIgnoreCase(redirectTo)) {
            return "redirect:/supports?updated=true";
        }
        return "redirect:/supports/" + id + "?updated=true";
    }

    @PostMapping("/{id}/files")
    public String uploadFiles(@PathVariable Long id,
                              @RequestParam(value = "files", required = false) List<MultipartFile> files) throws Exception {
        SupportTicket item = ticketService.findById(id);
        ticketService.create(item, files);
        return "redirect:/supports/" + id;
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
