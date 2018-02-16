package com.netgrif.workflow.petrinet.domain.dataset

import com.netgrif.workflow.petrinet.domain.I18nString
import org.springframework.data.mongodb.core.mapping.Document

@Document
class MultichoiceField extends ChoiceField<Set<I18nString>> {

    MultichoiceField() {
        super()
        super.setValue(new HashSet<I18nString>())
        super.setDefaultValue(new HashSet<I18nString>())
    }

    MultichoiceField(List<I18nString> values) {
        super(values)
        super.setValue(new HashSet<I18nString>())
        super.setDefaultValue(new HashSet<I18nString>())
    }

    @Override
    FieldType getType() {
        return FieldType.MULTICHOICE
    }

    void setDefaultValue(String value) {
        if (value == null) {
            this.defaultValue = null
        } else {
            String[] vls = value.split(",")
            def defaults = []
            vls.each { s ->
                defaults << choices.find { it ->
                    it.contains(s)
                }
            }
            super.setDefaultValue(defaults)
        }
    }

    void setValue(String value) {
        I18nString i18n = choices.find { it.contains(value) }
        if (i18n == null && value != null)
            throw new IllegalArgumentException("Value $value is not a choice")
        super.setValue([i18n] as Set)
    }

    void setValue(Collection<String> values) {
        def newValues = [] as Set
        for (String value : values) {
            I18nString i18n = choices.find { it.contains(value) }
            if (i18n == null && value != null)
                throw new IllegalArgumentException("Value $value is not a choice")
            newValues << i18n
        }
        super.setValue(newValues)
    }

    @Override
    void setValue(Set<I18nString> value) {
        super.setValue(value)
    }

    @Override
    void clearValue() {
        super.clearValue()
        setValue(getDefaultValue())
    }
}