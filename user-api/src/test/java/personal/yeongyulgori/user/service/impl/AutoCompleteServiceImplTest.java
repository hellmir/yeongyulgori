package personal.yeongyulgori.user.service.impl;

import org.apache.commons.collections4.Trie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import personal.yeongyulgori.user.service.AutoCompleteService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
class AutoCompleteServiceImplTest {

    @Autowired
    private Trie trie;

    @Autowired
    private AutoCompleteService autoCompleteService;

    @AfterEach
    void tearDown() {
        trie.clear();
    }

    @DisplayName("trie를 통해 사용자 성명을 자동 완성 단어로 등록할 수 있다.")
    @Test
    void addAutocompleteKeyWord() {

        // given
        String name1 = "홍길동";
        String name2 = "고길동";
        String name3 = "김길동";

        // when
        autoCompleteService.addAutocompleteKeyWord(name1);
        autoCompleteService.addAutocompleteKeyWord(name2);
        autoCompleteService.addAutocompleteKeyWord(name3);

        // then
        assertThat(trie).hasSize(3);

        assertThat(trie.containsKey("홍길동")).isTrue();
        assertThat(trie.containsKey("고길동")).isTrue();
        assertThat(trie.containsKey("김길동")).isTrue();

    }

    @DisplayName("키워드를 통해 자동완성에 등록된 단어들을 조회할 수 있다.")
    @Test
    void autocomplete() {

        // given
        String name1 = "홍길동";
        String name2 = "고길동";
        String name3 = "홍길춘";
        String name4 = "김길동";
        String name5 = "홍길순";

        autoCompleteService.addAutocompleteKeyWord(name1);
        autoCompleteService.addAutocompleteKeyWord(name2);
        autoCompleteService.addAutocompleteKeyWord(name3);
        autoCompleteService.addAutocompleteKeyWord(name4);
        autoCompleteService.addAutocompleteKeyWord(name5);

        // when
        List<String> autocompleteWords = autoCompleteService.autocomplete("홍길");

        // then
        assertThat(autocompleteWords).hasSize(3);
        assertThat(autocompleteWords).containsExactlyInAnyOrder("홍길동", "홍길춘", "홍길순");

    }

    @DisplayName("등록된 키워드를 삭제할 수 있다.")
    @Test
    void deleteAutocompleteKeyword() {

        // given
        String name1 = "홍길동";
        String name2 = "고길동";
        String name3 = "김길동";

        autoCompleteService.addAutocompleteKeyWord(name1);
        autoCompleteService.addAutocompleteKeyWord(name2);
        autoCompleteService.addAutocompleteKeyWord(name3);

        // when
        autoCompleteService.deleteAutocompleteKeyword(name2);

        // then
        assertThat(trie).hasSize(2);
        assertThat(trie.containsKey("홍길동")).isTrue();
        assertThat(trie.containsKey("고길동")).isFalse();
        assertThat(trie.containsKey("김길동")).isTrue();

    }

}