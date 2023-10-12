package personal.yeongyulgori.user.service.impl;


import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.Trie;
import org.springframework.stereotype.Service;
import personal.yeongyulgori.user.exception.serious.sub.AutoCompleteValueNotFoundException;
import personal.yeongyulgori.user.service.AutoCompleteService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AutoCompleteServiceImpl implements AutoCompleteService {

    private final Trie trie;

    @Override
    public void addAutoCompleteKeyWord(String fullName) {
        trie.put(fullName, "PRESENT");
    }

    @Override
    public List<String> autoComplete(String keyword) {

        List<String> autoCompleteResults = (List<String>) trie.prefixMap(keyword).keySet()
                .stream()
                .limit(10)
                .collect(Collectors.toList());

        return autoCompleteResults;

    }

    @Override
    public void deleteAutoCompleteKeyword(String fullName) {

        if (!trie.containsKey(fullName)) {
            throw new AutoCompleteValueNotFoundException("해당 자동완성 성명이 존재하지 않습니다. fullName: " + fullName);
        }

        trie.remove(fullName);

    }

}
