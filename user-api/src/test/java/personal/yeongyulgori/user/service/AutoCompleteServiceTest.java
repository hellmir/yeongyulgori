package personal.yeongyulgori.user.service;

import org.apache.commons.collections4.Trie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import personal.yeongyulgori.user.exception.serious.sub.KeywordNotFoundException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest
class AutoCompleteServiceTest {

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
    void addAutoCompleteKeyWord() {

        // given
        String name1 = "홍길동";
        String name2 = "고길동";
        String name3 = "김길동";

        // when
        autoCompleteService.addAutoCompleteKeyWord(name1);
        autoCompleteService.addAutoCompleteKeyWord(name2);
        autoCompleteService.addAutoCompleteKeyWord(name3);

        // then
        assertThat(trie).hasSize(3);

        assertThat(trie.containsKey("홍길동")).isTrue();
        assertThat(trie.containsKey("고길동")).isTrue();
        assertThat(trie.containsKey("김길동")).isTrue();

    }

    @DisplayName("키워드를 통해 자동완성에 등록된 단어들을 조회할 수 있다.")
    @Test
    void autoComplete() {

        // given
        String name1 = "홍길동";
        String name2 = "고길동";
        String name3 = "홍길춘";
        String name4 = "김길동";
        String name5 = "홍길순";

        autoCompleteService.addAutoCompleteKeyWord(name1);
        autoCompleteService.addAutoCompleteKeyWord(name2);
        autoCompleteService.addAutoCompleteKeyWord(name3);
        autoCompleteService.addAutoCompleteKeyWord(name4);
        autoCompleteService.addAutoCompleteKeyWord(name5);

        // when
        List<String> autoCompleteWords = autoCompleteService.autoComplete("홍길");

        // then
        assertThat(autoCompleteWords).hasSize(3);
        assertThat(autoCompleteWords).containsExactlyInAnyOrder("홍길동", "홍길춘", "홍길순");

    }

    @DisplayName("등록된 키워드를 삭제할 수 있다.")
    @Test
    void deleteAutoCompleteKeyword() {

        // given
        String name1 = "홍길동";
        String name2 = "고길동";
        String name3 = "김길동";

        autoCompleteService.addAutoCompleteKeyWord(name1);
        autoCompleteService.addAutoCompleteKeyWord(name2);
        autoCompleteService.addAutoCompleteKeyWord(name3);

        // when
        autoCompleteService.deleteAutoCompleteKeyword(name2);

        // then
        assertThat(trie).hasSize(2);
        assertThat(trie.containsKey("홍길동")).isTrue();
        assertThat(trie.containsKey("고길동")).isFalse();
        assertThat(trie.containsKey("김길동")).isTrue();

    }

    @DisplayName("존재하지 않는 키워드를 삭제하려고 하면 KeywordNotFoundException이 발생한다.")
    @Test
    void deleteAutoCompleteKeywordByNonExistKeyword() {

        // given
        String name1 = "홍길순";
        String name2 = "고길동";
        String name3 = "김길동";

        autoCompleteService.addAutoCompleteKeyWord(name2);
        autoCompleteService.addAutoCompleteKeyWord(name3);

        // when, then
        assertThatThrownBy(() -> autoCompleteService.deleteAutoCompleteKeyword("홍길동"))
                .isInstanceOf(KeywordNotFoundException.class)
                .hasMessage("해당 자동완성 키워드가 존재하지 않습니다. keyword: 홍길동");

    }

}
