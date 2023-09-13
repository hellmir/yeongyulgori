package personal.yeongyulgori.user.service;

import java.util.List;

public interface AutoCompleteService {

    void addAutocompleteKeyWord(String username);

    List<String> autocomplete(String keyword);

    void deleteAutocompleteKeyword(String keyword);

}
