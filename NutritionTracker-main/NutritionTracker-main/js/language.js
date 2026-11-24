
class LanguageManager {
    static currentLanguage = 'en';
    static translations = {};
    static supportedLanguages = ['en'];
    static initialized = false;

    static async init() {
        try {
            // English is the only supported language
            this.currentLanguage = 'en';

            // Load English translations
            const response = await fetch('./data/translations.json');

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();

            // Keep only English translations
            this.translations = {
                en: data.en || this.getDefaultTranslations().en
            };

            this.initialized = true;

            // Apply English to the UI
            this.applyLanguage();

            console.log('Language manager initialized with English');

        } catch (error) {
            console.error('Error loading translations:', error);

            // Fallback to default English translations
            this.translations = this.getDefaultTranslations();
            this.initialized = true;

            this.applyLanguage();
        }
    }

    static getDefaultTranslations() {
        return {
            en: {
                'app.title': 'Diet Planner',
                'nav.today': 'Today',
                'nav.weekly': 'Weekly',
                'nav.foods': 'Foods',
                'nav.stats': 'Stats',

                'summary.calories': 'Calories',
                'summary.protein': 'Protein',
                'summary.carbs': 'Carbs',
                'summary.fat': 'Fat',

                'meals.breakfast': 'Breakfast',
                'meals.lunch': 'Lunch',
                'meals.dinner': 'Dinner',
                'meals.snacks': 'Snacks',
                'meals.empty': 'No foods added yet',

                'foods.title': 'Food Database',
                'foods.search': 'Search foods...',
                'foods.no_results': 'No foods found',

                'modal.add_food': 'Add Food',
                'modal.portion': 'Portion (g)',
                'modal.add': 'Add to Meal',

                'stats.title': 'Nutrition Statistics',
                'stats.daily_intake': 'Daily Intake',
                'stats.weekly_trend': 'Weekly Trend',

                'weekly.items': 'items',
                'weekly.no_meals': 'No meals planned',

                'charts.days': 'Days',
                'charts.macros': 'Macros (g)',
                'charts.consumed': 'Consumed',
                'charts.remaining': 'Remaining',
                'charts.excess': 'Excess',

                'days.monday': 'Monday',
                'days.tuesday': 'Tuesday',
                'days.wednesday': 'Wednesday',
                'days.thursday': 'Thursday',
                'days.friday': 'Friday',
                'days.saturday': 'Saturday',
                'days.sunday': 'Sunday',

                'days.mon': 'Mon',
                'days.tue': 'Tue',
                'days.wed': 'Wed',
                'days.thu': 'Thu',
                'days.fri': 'Fri',
                'days.sat': 'Sat',
                'days.sun': 'Sun'
            }
        };
    }

    static translate(key, fallback = key) {
        if (!this.initialized) {
            return fallback;
        }

        return this.translations.en?.[key] || fallback;
    }

    static applyLanguage() {
        // English uses left-to-right layout
        document.documentElement.dir = 'ltr';
        document.documentElement.lang = 'en';

        // Update current language display
        const languageDisplay = document.getElementById('currentLanguage');

        if (languageDisplay) {
            languageDisplay.textContent = 'EN';
        }

        // Apply translations to UI elements
        document.querySelectorAll('[data-translate]').forEach(element => {
            const key = element.getAttribute('data-translate');
            element.textContent = this.translate(key);
        });

        // Apply translations to placeholders
        document.querySelectorAll('[data-translate-placeholder]').forEach(element => {
            const key = element.getAttribute('data-translate-placeholder');
            element.placeholder = this.translate(key);
        });

        // Remove RTL styling if previously applied
        document.body.classList.remove('rtl');

        const rtlStyles = document.getElementById('rtl-styles');

        if (rtlStyles) {
            rtlStyles.remove();
        }

        // Update charts if available
        if (
            window.ChartsManager &&
            window.ChartsManager.updateCharts
        ) {
            setTimeout(() => {
                window.ChartsManager.updateCharts();
            }, 100);
        }

        console.log('Applied English language to UI');
    }

    // English-only language selection
    static toggleLanguage() {
        return this.setLanguage('en');
    }

    static setLanguage(language) {
        if (language !== 'en') {
            console.warn(`Unsupported language: ${language}`);
            return false;
        }

        this.currentLanguage = 'en';

        StorageManager.saveLanguage('en');

        this.applyLanguage();

        window.dispatchEvent(
            new CustomEvent('languageChanged', {
                detail: { language: 'en' }
            })
        );

        return true;
    }

    static getCurrentLanguage() {
        return 'en';
    }

    static getSupportedLanguages() {
        return ['en'];
    }

    static isRTL() {
        return false;
    }

    static formatNumber(number, options = {}) {
        const defaults = {
            minimumFractionDigits: 0,
            maximumFractionDigits: 1
        };

        const formatOptions = {
            ...defaults,
            ...options
        };

        try {
            return new Intl.NumberFormat(
                'en',
                formatOptions
            ).format(number);

        } catch (error) {
            return number.toFixed(
                formatOptions.maximumFractionDigits
            );
        }
    }

    static formatDate(date, options = {}) {
        const defaults = {
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        };

        const formatOptions = {
            ...defaults,
            ...options
        };

        try {
            return new Intl.DateTimeFormat(
                'en',
                formatOptions
            ).format(date);

        } catch (error) {
            return date.toLocaleDateString('en');
        }
    }

    static addTranslation(language, key, value) {
        if (language !== 'en') {
            console.warn('Only English translations are supported.');
            return;
        }

        if (!this.translations.en) {
            this.translations.en = {};
        }

        this.translations.en[key] = value;
    }

    static addTranslations(language, translations) {
        if (language !== 'en') {
            console.warn('Only English translations are supported.');
            return;
        }

        if (!this.translations.en) {
            this.translations.en = {};
        }

        Object.assign(this.translations.en, translations);
    }

    static exportTranslations() {
        return {
            translations: {
                en: this.translations.en || {}
            },
            currentLanguage: 'en',
            supportedLanguages: ['en'],
            exportDate: new Date().toISOString(),
            version: '1.0'
        };
    }

    static importTranslations(data) {
        try {
            if (data.translations?.en) {
                this.translations = {
                    en: {
                        ...this.translations.en,
                        ...data.translations.en
                    }
                };
            }

            this.currentLanguage = 'en';
            this.supportedLanguages = ['en'];

            this.applyLanguage();

            return true;

        } catch (error) {
            console.error('Error importing translations:', error);
            return false;
        }
    }

    static validateTranslations() {
        const issues = [];

        const baseLanguage = 'en';

        if (!this.translations.en) {
            issues.push("Base language 'en' not found");
            return issues;
        }

        // Check whether English translations are available
        Object.keys(this.getDefaultTranslations().en).forEach(key => {
            if (!(key in this.translations.en)) {
                issues.push(`Missing English translation: '${key}'`);
            }
        });

        return issues;
    }

    static getTranslationProgress() {
        const totalKeys = Object.keys(
            this.getDefaultTranslations().en
        ).length;

        const translatedKeys = Object.keys(
            this.translations.en || {}
        ).length;

        return {
            en: totalKeys
                ? Math.min(100, Math.round(
                    (translatedKeys / totalKeys) * 100
                ))
                : 0
        };
    }

    static translateElement(element, key, fallback = null) {
        const translation = this.translate(key, fallback);

        element.textContent = translation;

        return translation;
    }

    static translateAttribute(element, attribute, key, fallback = null) {
        const translation = this.translate(key, fallback);

        element.setAttribute(attribute, translation);

        return translation;
    }

    static pluralize(key, count, fallback = null) {
        if (count === 1) {
            return this.translate(
                key + '_singular',
                fallback || key
            );
        }

        return this.translate(
            key + '_plural',
            fallback || key
        );
    }
}

// Global language manager instance
window.LanguageManager = LanguageManager;

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    if (!LanguageManager.initialized) {
        LanguageManager.init();
    }
});
