package com.therapy.coredataservice.repository;

import com.therapy.coredataservice.entity.AnimationTrigger;
import com.therapy.coredataservice.entity.CharacterEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:character_repo_test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class CharacterCatalogRepositoryTest {

    @Autowired
    private CharacterRepository characterRepository;

    @Autowired
    private AnimationTriggerRepository animationTriggerRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldPersistAndRetrieveAnimationTriggerWithHebrewFields() {
        AnimationTrigger trigger = new AnimationTrigger();
        trigger.setAnimationTriggerId("WAVE_HELLO");
        trigger.setDisplayName("נפנוף לשלום");
        trigger.setAiUsageDescription("השתמש בתחילת שיחה או בפרידה מהילד");

        animationTriggerRepository.saveAndFlush(trigger);
        entityManager.clear();

        Optional<AnimationTrigger> loaded = animationTriggerRepository.findById("WAVE_HELLO");
        assertThat(loaded).isPresent();
        assertThat(loaded.get().getAnimationTriggerId()).isEqualTo("WAVE_HELLO");
        assertThat(loaded.get().getDisplayName()).isEqualTo("נפנוף לשלום");
        assertThat(loaded.get().getAiUsageDescription()).isEqualTo("השתמש בתחילת שיחה או בפרידה מהילד");
    }

    @Test
    void shouldPersistCharacterEntityWithSharedAnimationTriggers() {
        AnimationTrigger wave = animationTriggerRepository.save(
                new AnimationTrigger("WAVE_HELLO", "נפנוף לשלום", "השתמש בתחילת שיחה או בפרידה מהילד")
        );
        AnimationTrigger listen = animationTriggerRepository.save(
                new AnimationTrigger("IDLE_LISTEN", "הקשבה", "השתמש כאשר הדמות מקשיבה או שואלת שאלה רגועה")
        );

        CharacterEntity astro = new CharacterEntity();
        astro.setId("char_astro");
        astro.setName("אסטרו החוקר מהחלל");
        astro.setDescription("דמות סקרנית, חמה ומעודדת המדברת בשפה מותאמת לילדים");
        astro.setPersonalityProfilePrompt("אתה אסטרו, חבר סקרן, חם ואמפתי.");
        astro.setVoiceConfigId("voice_astro_he_01");
        astro.setUnityModelAssetId("Astro3DModel");
        astro.setAvailableAnimations(List.of(wave, listen));

        CharacterEntity rex = new CharacterEntity(
                "char_rex",
                "רקס הדינוזאור הידידותי",
                "דינוזאור ידידותי ורגוע",
                "אתה רקס, דינוזאור ידידותי.",
                "voice_rex_he_01",
                "Rex3DModel",
                List.of(wave, listen)
        );

        characterRepository.saveAllAndFlush(List.of(astro, rex));
        entityManager.clear();

        CharacterEntity loadedAstro = characterRepository.findById("char_astro").orElseThrow();
        CharacterEntity loadedRex = characterRepository.findById("char_rex").orElseThrow();

        assertThat(loadedAstro.getName()).isEqualTo("אסטרו החוקר מהחלל");
        assertThat(loadedAstro.getDescription()).isEqualTo("דמות סקרנית, חמה ומעודדת המדברת בשפה מותאמת לילדים");
        assertThat(loadedAstro.getPersonalityProfilePrompt()).isEqualTo("אתה אסטרו, חבר סקרן, חם ואמפתי.");
        assertThat(loadedAstro.getVoiceConfigId()).isEqualTo("voice_astro_he_01");
        assertThat(loadedAstro.getUnityModelAssetId()).isEqualTo("Astro3DModel");
        assertThat(loadedAstro.getAvailableAnimations())
                .extracting(AnimationTrigger::getAnimationTriggerId)
                .containsExactlyInAnyOrder("WAVE_HELLO", "IDLE_LISTEN");

        assertThat(loadedRex.getAvailableAnimations())
                .extracting(AnimationTrigger::getAnimationTriggerId)
                .containsExactlyInAnyOrder("WAVE_HELLO", "IDLE_LISTEN");
    }

    @Test
    void deletingCharacterShouldNotDeleteSharedAnimationTriggers() {
        AnimationTrigger wave = animationTriggerRepository.save(
                new AnimationTrigger("WAVE_HELLO", "נפנוף לשלום", "השתמש בתחילת שיחה")
        );

        CharacterEntity character = new CharacterEntity(
                "char_temp",
                "דמות זמנית",
                "תיאור",
                "פרומפט",
                "voice_temp",
                "TempModel",
                List.of(wave)
        );
        characterRepository.saveAndFlush(character);
        entityManager.clear();

        characterRepository.deleteById("char_temp");
        characterRepository.flush();
        entityManager.clear();

        assertThat(characterRepository.findById("char_temp")).isEmpty();
        assertThat(animationTriggerRepository.findById("WAVE_HELLO")).isPresent();
    }
}
