package personal.yeongyulgori.user.service;

import java.util.List;

public interface AutoCompleteService {

    void addAutoCompleteKeyWord(String username);

    List<String> autoComplete(String keyword);

    void deleteAutoCompleteKeyword(String keyword);

}
