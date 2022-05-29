package com.learn.inboxapp.controllers;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.learn.inboxapp.email.Email;
import com.learn.inboxapp.email.EmailRepository;
import com.learn.inboxapp.emailslist.EmailsList;
import com.learn.inboxapp.emailslist.EmailsListPrimaryKey;
import com.learn.inboxapp.emailslist.EmailsListRepository;
import com.learn.inboxapp.folders.Folder;
import com.learn.inboxapp.folders.FolderRepository;
import com.learn.inboxapp.folders.FoldersService;
import com.learn.inboxapp.folders.UnreadEmailStatsRepository;

@Controller
public class EmailPageController {

	@Autowired
	private FolderRepository folderRepository;

	@Autowired
	private EmailRepository emailRepository;

	@Autowired
	private EmailsListRepository emailsListRepository;

	@Autowired
	private UnreadEmailStatsRepository unreadEmailStatsRepository;
	@Autowired
	private FoldersService foldersService;

	@GetMapping(value = "/email/{id}")
	public String getEmailPage(@PathVariable String id, @RequestParam String folder, @AuthenticationPrincipal OAuth2User principal, Model model) {
		if (principal != null && principal.getAttribute("login") != null) {
			String loginId = principal.getAttribute("login");
			List<Folder> folders = folderRepository.findAllById(loginId);
			List<Folder> initFolders = foldersService.init(loginId);
			// initFolders.stream().forEach(folderRepository::save);
			model.addAttribute("defaultFolders", initFolders);
			
			if (folders.size() > 0) {
				model.addAttribute("userFolders", folders);
			}
			model.addAttribute("userName", principal.getAttribute("name"));
			try {
				UUID uuid = UUID.fromString(id);
				Optional<Email> optionalEmail = emailRepository.findById(uuid);
				if (optionalEmail.isPresent()) {
					Email email = optionalEmail.get();
					String toIds = String.join(",", email.getTo());
					model.addAttribute("email", optionalEmail.get());
					model.addAttribute("toIds", toIds);
					
					EmailsListPrimaryKey key = new EmailsListPrimaryKey();
					key.setUserId(loginId);
					key.setLabel(folder);
					key.setTimeId(email.getId());
					
					Optional<EmailsList> optionalEmailListItem = emailsListRepository.findById(key);
					if (!optionalEmailListItem.isPresent())
						throw new IllegalArgumentException();
					
					EmailsList emailListItem = optionalEmailListItem.get();
					if (!emailListItem.isRead()) {
						unreadEmailStatsRepository.decrementUnreadCounter(loginId, folder);
					}
					emailListItem.setRead(true);
					emailsListRepository.save(emailListItem);
					Map<String, Integer> folderToUnreadCounts = foldersService.getUnreadCountsMap(loginId);
					model.addAttribute("folderToUnreadCounts", folderToUnreadCounts);
					return "email-page";
				}
			} catch (IllegalArgumentException e) {
				return "inbox-page";
			}
		}
		return "index";
	}
}
