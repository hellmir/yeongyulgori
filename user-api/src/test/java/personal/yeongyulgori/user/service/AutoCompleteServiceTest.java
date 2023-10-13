package personal.yeongyulgori.user.service;

import org.apache.commons.collections4.Trie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import personal.yeongyulgori.user.exception.serious.sub.AutoCompleteValueNotFoundException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static personal.yeongyulgori.user.testutil.TestConstant.*;

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

        // given, when
        autoCompleteService.addAutoCompleteKeyWord(FULL_NAME1);
        autoCompleteService.addAutoCompleteKeyWord(FULL_NAME2);
        autoCompleteService.addAutoCompleteKeyWord(FULL_NAME3);

        // then
        assertThat(trie).hasSize(3);

        assertThat(trie.containsKey(FULL_NAME1)).isTrue();
        assertThat(trie.containsKey(FULL_NAME2)).isTrue();
        assertThat(trie.containsKey(FULL_NAME3)).isTrue();

    }

    @DisplayName("키워드를 통해 자동완성에 등록된 단어들을 조회할 수 있다.")
    @Test
    void autoComplete() {

        // given
        autoCompleteService.addAutoCompleteKeyWord(FULL_NAME1);
        autoCompleteService.addAutoCompleteKeyWord(FULL_NAME2);
        autoCompleteService.addAutoCompleteKeyWord(FULL_NAME5);
        autoCompleteService.addAutoCompleteKeyWord(FULL_NAME3);
        autoCompleteService.addAutoCompleteKeyWord(FULL_NAME6);

        // when
        List<String> autoCompleteWords = autoCompleteService.autoComplete(FRONT_PART_OF_NAME);

        // then
        assertThat(autoCompleteWords).hasSize(3);
        assertThat(autoCompleteWords).containsExactlyInAnyOrder(FULL_NAME1, FULL_NAME5, FULL_NAME6);

    }

    @DisplayName("등록된 키워드를 삭제할 수 있다.")
    @Test
    void deleteAutoCompleteKeyword() {

        // given
        autoCompleteService.addAutoCompleteKeyWord(FULL_NAME1);
        autoCompleteService.addAutoCompleteKeyWord(FULL_NAME2);
        autoCompleteService.addAutoCompleteKeyWord(FULL_NAME3);

        // when
        autoCompleteService.deleteAutoCompleteKeyword(FULL_NAME2);

        // then
        assertThat(trie).hasSize(2);
        assertThat(trie.containsKey(FULL_NAME1)).isTrue();
        assertThat(trie.containsKey(FULL_NAME2)).isFalse();
        assertThat(trie.containsKey(FULL_NAME3)).isTrue();

    }

    @DisplayName("존재하지 않는 키워드를 삭제하려고 하면 KeywordNotFoundException이 발생한다.")
    @Test
    void deleteAutoCompleteKeywordByNonExistKeyword() {

        // given
        autoCompleteService.addAutoCompleteKeyWord(FULL_NAME5);
        autoCompleteService.addAutoCompleteKeyWord(FULL_NAME2);
        autoCompleteService.addAutoCompleteKeyWord(FULL_NAME3);

        // when, then
        assertThatThrownBy(() -> autoCompleteService.deleteAutoCompleteKeyword(FULL_NAME1))
                .isInstanceOf(AutoCompleteValueNotFoundException.class)
                .hasMessage("해당 자동완성 성명이 존재하지 않습니다. fullName: " + FULL_NAME1);

    }

}
