package personal.yeongyulgori.user.service.impl;


import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.Trie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import personal.yeongyulgori.user.service.AutoCompleteService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AutoCompleteServiceImpl implements AutoCompleteService {

    private final Trie trie;

    private static final Logger log = LoggerFactory.getLogger(AutoCompleteService.class);

    @Override
    public void addAutoCompleteKeyWord(String name) {

        log.info("Add autoComplete keyword by username: " + name);

        trie.put(name, null);

    }

    @Override
    public List<String> autoComplete(String keyword) {

        log.info("Beginning to retrieve autoComplete results by keyword: " + keyword);

        StopWatch stopWatch = new StopWatch();

        stopWatch.start();

        List<String> autoCompleteResults = (List<String>) trie.prefixMap(keyword).keySet()
                .stream()
                .limit(10)
                .collect(Collectors.toList());

        stopWatch.stop();

        log.info("AutoComplete results retrieved successfully: {}\n Retrieving task execution time: {} ms",
                keyword, stopWatch.getTotalTimeMillis());

        return autoCompleteResults;

    }

    @Override
    public void deleteAutoCompleteKeyword(String keyword) {

        log.info("Delete autoComplete keyword: " + keyword);

        trie.remove(keyword);

    }

}
