package com.learn.inboxapp.folders;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FoldersService {

	@Autowired
    private UnreadEmailStatsRepository unreadEmailStatsRepository;

    public List<Folder> init(String userId) {
        List<Folder> defaultFolders = Arrays.asList(
            new Folder(userId, "Inbox", "blue"),
            new Folder(userId, "Sent", "purple"),
            new Folder(userId, "Important", "red"),
            new Folder(userId, "Done", "green")
        );
        return defaultFolders;
    }

    public Map<String, Integer> getUnreadCountsMap(String loginId) {
        List<UnreadEmailStats> unreadStats = unreadEmailStatsRepository.findAllById(loginId);
        Map<String, Integer> folderToUnreadCounts = unreadStats.stream()
                .collect(Collectors.toMap(UnreadEmailStats::getLabel, UnreadEmailStats::getUnreadCount));
        return folderToUnreadCounts;
    }
    
}
