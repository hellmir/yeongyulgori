package personal.yeongyulgori.user.service.impl;


import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.Trie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import personal.yeongyulgori.user.service.AutoCompleteService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AutoCompleteServiceImpl implements AutoCompleteService {

    private final Trie trie;

    private static final Logger log = LoggerFactory.getLogger(AutoCompleteService.class);

    @Override
    public void addAutocompleteKeyWord(String name) {

        log.info("Add autocomplete keyword by username: " + name);

        trie.put(name, null);

    }

    @Override
    public List<String> autocomplete(String keyword) {

        log.info("Retrieve autocomplete keyword: " + keyword);

        return (List<String>) trie.prefixMap(keyword).keySet()
                .stream()
                .limit(10)
                .collect(Collectors.toList());

    }

    @Override
    public void deleteAutocompleteKeyword(String keyword) {

        log.info("Delete autocomplete keyword: " + keyword);

        trie.remove(keyword);

    }

}
