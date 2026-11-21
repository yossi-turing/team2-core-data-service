package com.therapy.coredataservice.config;

import com.therapy.coredataservice.entity.AnimationTrigger;
import com.therapy.coredataservice.entity.CharacterEntity;
import com.therapy.coredataservice.repository.AnimationTriggerRepository;
import com.therapy.coredataservice.repository.CharacterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class CharacterDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CharacterDataSeeder.class);

    private final CharacterRepository characterRepository;
    private final AnimationTriggerRepository animationTriggerRepository;

    public CharacterDataSeeder(
            CharacterRepository characterRepository,
            AnimationTriggerRepository animationTriggerRepository
    ) {
        this.characterRepository = characterRepository;
        this.animationTriggerRepository = animationTriggerRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        List<AnimationTrigger> animations = seedAnimationsIfEmpty();
        seedCharactersIfEmpty(animations);
    }

    private List<AnimationTrigger> seedAnimationsIfEmpty() {
        if (animationTriggerRepository.count() > 0) {
            log.debug("Animation triggers already exist in DB, skipping animation seeding.");
            return animationTriggerRepository.findAll();
        }

        List<AnimationTrigger> defaultAnimations = List.of(
                new AnimationTrigger(
                        "WAVE_HELLO",
                        "נפנוף לשלום",
                        "השתמש בתחילת שיחה או בפרידה מהילד"
                ),
                new AnimationTrigger(
                        "IDLE_LISTEN",
                        "הקשבה",
                        "השתמש כאשר הדמות מקשיבה או שואלת שאלה רגועה"
                ),
                new AnimationTrigger(
                        "THINKING",
                        "חשיבה",
                        "השתמש כאשר הדמות מהרהרת או מתלבטת יחד עם הילד"
                ),
                new AnimationTrigger(
                        "CLAP_HAPPY",
                        "מחיאת כף",
                        "השתמש לשמחה, התלהבות וחיזוק חיובי לילד"
                ),
                new AnimationTrigger(
                        "EMPATHIC_NOD",
                        "הנהון אמפתי",
                        "השתמש כדי לשדר הבנה, הכלה ואמפתיה כשהילד משתף רגש מורכב או קושי"
                ),
                new AnimationTrigger(
                        "HUG_GESTURE",
                        "מחוות חיבוק",
                        "השתמש לעידוד רגשי עמוק, נחמה ומתן תחושת חום וביטחון לילד"
                )
        );

        List<AnimationTrigger> savedAnimations = animationTriggerRepository.saveAll(defaultAnimations);
        log.info("Seeded {} default animation triggers.", savedAnimations.size());
        return savedAnimations;
    }

    private void seedCharactersIfEmpty(List<AnimationTrigger> availableAnimations) {
        if (characterRepository.count() > 0) {
            log.debug("Characters already exist in DB, skipping character seeding.");
            return;
        }

        List<CharacterEntity> defaultCharacters = List.of(
                new CharacterEntity(
                        "char_astro",
                        "אסטרו החוקר מהחלל",
                        "דמות סקרנית, חמה ומעודדת המדברת בשפה מותאמת לילדים",
                        "אתה אסטרו, חוקר חלל סקרן, חם ואמפתי. דבר במשפטים קצרים, ברורים ומותאמים לילדים, "
                                + "בטון מעודד ומכיל. עודד את הילד לשתף רגשות וחוויות באמצעות סקרנות ומשחקיות, "
                                + "ללא שיפוטיות. ביטויים אופייניים: 'איזו תגלית מדהימה!', 'בוא נחקור את זה יחד כמו צוות חלל!'.",
                        "voice_astro_he_01",
                        "Astro3DModel",
                        availableAnimations
                ),
                new CharacterEntity(
                        "char_rex",
                        "רקס הדינוזאור הידידותי",
                        "דינוזאור שובב, רגוע ואוהב משחקים שעוזר לילדים להרגיש אמיצים ובטוחים",
                        "אתה רקס, דינוזאור ידידותי, עדין ומצחיק בעדינות. דבר בשפה פשוטה, משחקית ומעודדת. "
                                + "עזור לילד להתמודד עם פחדים ותסכול בצעדים קטנים ובקצב שלו, תוך מתן חיזוקים חיוביים. "
                                + "ביטויים אופייניים: 'צעד קטן של דינוזאור גדול!', 'גם דינוזאורים מרגישים ככה לפעמים!'.",
                        "voice_rex_he_01",
                        "Rex3DModel",
                        availableAnimations
                ),
                new CharacterEntity(
                        "char_leo",
                        "ליאו האריה האמפתי",
                        "אריה עדין, סבלני וקשוב שמשרה תחושת רוגע וביטחון בשיחות רגשיות",
                        "אתה ליאו, אריה רגוע, סבלני ובעל לב רחב. דבר ברוך, בנחת ובאמפתיה עמוקה. "
                                + "תן תוקף לרגשות של הילד, עזור לו לזהות ולשיים תחושות, והצע דרכי הרגעה עדינות. "
                                + "ביטויים אופייניים: 'לכל רגש יש מקום אצלי', 'איזה אומץ יש לך שסיפרת לי'.",
                        "voice_leo_he_01",
                        "Leo3DModel",
                        availableAnimations
                )
        );

        List<CharacterEntity> savedCharacters = characterRepository.saveAll(defaultCharacters);
        log.info("Seeded {} default characters.", savedCharacters.size());
    }
}
