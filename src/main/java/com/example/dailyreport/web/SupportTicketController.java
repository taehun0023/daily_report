package com.example.dailyreport.web;

import com.example.dailyreport.domain.RequestFile;
import com.example.dailyreport.domain.SupportTicket;
import com.example.dailyreport.domain.TicketStatus;
import com.example.dailyreport.domain.UserAccount;
import com.example.dailyreport.domain.UserRole;
import com.example.dailyreport.repository.RequestFileRepository;
import com.example.dailyreport.repository.UserAccountRepository;
import com.example.dailyreport.service.CurrentUserProvider;
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
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/supports")
public class SupportTicketController {
    private static final List<String> INQUIRY_TYPES = Arrays.asList(
            "로그인", "계정", "결제", "환불", "배송", "기술", "기타"
    );
    private static final List<String> CATEGORIES = Arrays.asList(
            "계정", "결제", "배송", "서비스", "기술", "기타"
    );

    private final SupportTicketService ticketService;
    private final RequestFileRepository fileRepository;
    private final FileStorageService fileStorageService;
    private final UserAccountRepository userRepository;
    private final CurrentUserProvider currentUserProvider;

    public SupportTicketController(SupportTicketService ticketService,
                             RequestFileRepository fileRepository,
                             FileStorageService fileStorageService,
                             UserAccountRepository userRepository,
                             CurrentUserProvider currentUserProvider) {
        this.ticketService = ticketService;
        this.fileRepository = fileRepository;
        this.fileStorageService = fileStorageService;
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    public String list(@RequestParam(required = false) TicketStatus status,
                       @RequestParam(required = false) Long userId,
                       @RequestParam(required = false) String inquiryType,
                       @RequestParam(required = false) String category,
                       @RequestParam(required = false) LocalDate fromDate,
                       @RequestParam(required = false) LocalDate toDate,
                       Model model) {
        UserAccount currentUser = currentUserProvider.getCurrentUser();
        boolean isAdmin = currentUser != null && currentUser.getRole() == UserRole.ADMIN;
        Long effectiveUserId = isAdmin ? userId : (currentUser != null ? currentUser.getId() : null);

        model.addAttribute("tickets", ticketService.findFiltered(status, effectiveUserId, inquiryType, category, fromDate, toDate));
        model.addAttribute("statuses", TicketStatus.values());
        model.addAttribute("users", userRepository.findAll()
                .stream()
                .filter(u -> currentUser == null || !u.getId().equals(currentUser.getId()))
                .toList());
        model.addAttribute("inquiryTypes", INQUIRY_TYPES);
        model.addAttribute("categories", CATEGORIES);
        model.addAttribute("status", status);
        model.addAttribute("userId", effectiveUserId);
        model.addAttribute("inquiryType", inquiryType);
        model.addAttribute("category", category);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("selectedUser",
                effectiveUserId != null ? userRepository.findById(effectiveUserId).orElse(null) : null);
        return "supports/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        UserAccount currentUser = currentUserProvider.getCurrentUser();
        boolean isAdmin = currentUser != null && currentUser.getRole() == UserRole.ADMIN;
        model.addAttribute("ticket", new SupportTicket());
        model.addAttribute("users", userRepository.findAll()
                .stream()
                .filter(u -> currentUser == null || !u.getId().equals(currentUser.getId()))
                .toList());
        model.addAttribute("inquiryTypes", INQUIRY_TYPES);
        model.addAttribute("categories", CATEGORIES);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("currentUser", currentUser);
        return "supports/form";
    }

    @PostMapping
    public String create(@ModelAttribute SupportTicket ticket,
                         @RequestParam(value = "userId", required = false) Long userId,
                         @RequestParam(value = "files", required = false) List<MultipartFile> files,
                         Model model) throws Exception {
        UserAccount currentUser = currentUserProvider.getCurrentUser();
        boolean isAdmin = currentUser != null && currentUser.getRole() == UserRole.ADMIN;
        if (userRepository.findAll().isEmpty()) {
            model.addAttribute("ticket", ticket);
            model.addAttribute("users", userRepository.findAll()
                    .stream()
                    .filter(u -> currentUser == null || !u.getId().equals(currentUser.getId()))
                    .toList());
            model.addAttribute("inquiryTypes", INQUIRY_TYPES);
            model.addAttribute("categories", CATEGORIES);
            model.addAttribute("isAdmin", isAdmin);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("error", "담당자 계정이 없습니다. 먼저 사용자(담당자)를 등록하세요.");
            return "supports/form";
        }
        if (userId == null) {
            model.addAttribute("ticket", ticket);
            model.addAttribute("users", userRepository.findAll()
                    .stream()
                    .filter(u -> currentUser == null || !u.getId().equals(currentUser.getId()))
                    .toList());
            model.addAttribute("inquiryTypes", INQUIRY_TYPES);
            model.addAttribute("categories", CATEGORIES);
            model.addAttribute("isAdmin", isAdmin);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("error", "담당자를 선택해야 합니다.");
            return "supports/form";
        }
        ticket.setUser(userRepository.getReferenceById(userId));
        try {
            ticketService.create(ticket, files);
        } catch (IllegalArgumentException e) {
            model.addAttribute("ticket", ticket);
            model.addAttribute("users", userRepository.findAll()
                    .stream()
                    .filter(u -> currentUser == null || !u.getId().equals(currentUser.getId()))
                    .toList());
            model.addAttribute("inquiryTypes", INQUIRY_TYPES);
            model.addAttribute("categories", CATEGORIES);
            model.addAttribute("isAdmin", isAdmin);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("error", e.getMessage());
            return "supports/form";
        }
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
        UserAccount currentUser = currentUserProvider.getCurrentUser();
        boolean isAdmin = currentUser != null && currentUser.getRole() == UserRole.ADMIN;
        model.addAttribute("ticket", ticketService.findById(id));
        model.addAttribute("users", userRepository.findAll()
                .stream()
                .filter(u -> currentUser == null || !u.getId().equals(currentUser.getId()))
                .toList());
        model.addAttribute("inquiryTypes", INQUIRY_TYPES);
        model.addAttribute("categories", CATEGORIES);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("currentUser", currentUser);
        return "supports/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute SupportTicket ticket,
                         @RequestParam(value = "userId", required = false) Long userId,
                         Model model) {
        UserAccount currentUser = currentUserProvider.getCurrentUser();
        boolean isAdmin = currentUser != null && currentUser.getRole() == UserRole.ADMIN;
        if (userId != null) {
            ticket.setUser(userRepository.getReferenceById(userId));
        }
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
