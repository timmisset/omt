package com.misset.opp.omt;

import com.intellij.lang.Language;
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable;
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider;
import org.jetbrains.annotations.NotNull;

public class OMTLanguageCodeStyleSettingsProvider extends LanguageCodeStyleSettingsProvider {
    @NotNull
    @Override
    public Language getLanguage() {
        return OMTLanguage.INSTANCE;
    }

    @Override
    public void customizeSettings(@NotNull CodeStyleSettingsCustomizable consumer, @NotNull SettingsType settingsType) {
        if (settingsType == SettingsType.SPACING_SETTINGS) {
            consumer.showStandardOptions("SPACE_AROUND_ASSIGNMENT_OPERATORS");
            consumer.renameStandardOption("SPACE_AROUND_ASSIGNMENT_OPERATORS", "Separator");
        } else if (settingsType == SettingsType.BLANK_LINES_SETTINGS) {
            consumer.showStandardOptions("KEEP_BLANK_LINES_IN_CODE");
        }
    }

    @Override
    public String getCodeSample(@NotNull SettingsType settingsType) {
        return "import:\n" +
                "    ../handmatig/handmatig.activity.omt:\n" +
                "    -   PersoonHandmatigWijzigen\n" +
                "    ../util/verwijder-dossier-persoon.omt:\n" +
                "    -   verwijderDossierPersoon\n" +
                "    ../util/queries.omt:\n" +
                "    -   gesorteerdePersonen\n" +
                "    '../util/voeg-persoon-toe.procedure.omt':\n" +
                "    -   ToevoegenPersoon\n" +
                "    '@client/material/src/bevestig/bevestig.activity.omt':\n" +
                "    -   Bevestig\n" +
                "    '@client/registratie/src/utils/queries.omt':\n" +
                "    -   hoedanigheidSoortGraph\n" +
                "\n" +
                "prefixes:\n" +
                "    pol:    <http://ontologie.politie.nl/def/politie#>\n" +
                "    xsd:    <http://www.w3.org/2001/XMLSchema#>\n" +
                "    rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "    owl:    <http://www.w3.org/2002/07/owl#>\n" +
                "\n" +
                "queries: |\n" +
                "    DEFINE QUERY alleHoedanigheidSoorten() => / pol:PartijHoedanigheidSoort / ^rdf:type[\n" +
                "                                    pol:geldigBeginDatum / IF_EMPTY('1800-01-01' / CAST(/xsd:date)) <= CURRENT_DATE() AND\n" +
                "                                    CURRENT_DATE() <= pol:geldigEindDatum / IF_EMPTY(CURRENT_DATE)];\n" +
                "\n" +
                "model:\n" +
                "    ToonPersonen: !Activity\n" +
                "        title: Persoon\n" +
                "\n" +
                "        params:\n" +
                "        -   $instantie\n" +
                "        -   $partijHoedanigheidType (owl:Thing)\n" +
                "        -   $entiteitPersoonAttribuut\n" +
                "\n" +
                "        variables:\n" +
                "        -   $entiteitPersoonAttribuutString = $entiteitPersoonAttribuut / CAST(/xsd:string)\n" +
                "\n" +
                "        queries: |\n" +
                "            DEFINE QUERY hoedanighedenQuery => $instantie / TRAVERSE($entiteitPersoonAttribuutString, false);\n" +
                "            DEFINE QUERY geselecteerdeHoedanigheden($partij) =>\n" +
                "                                hoedanighedenQuery()[(pol:partij == $partij AND (pol:hoedanigheidSoort / EXISTS))];\n" +
                "            DEFINE QUERY nietGeselecteerdeHoedanigheidSoorten($partij) =>\n" +
                "                                alleHoedanigheidSoorten()[NOT IN(geselecteerdeHoedanigheden($partij) / pol:hoedanigheidSoort)];\n" +
                "            DEFINE QUERY gesorteerdeGeselecteerdeHoedanigheden($partij) =>\n" +
                "                                geselecteerdeHoedanigheden($partij) / ORDER_BY(pol:hoedanigheidSoort / pol:omschrijving);\n" +
                "            DEFINE QUERY gesorteerdeNietGeselecteerdeHoedanigheidSoorten($partij) =>\n" +
                "                                nietGeselecteerdeHoedanigheidSoorten($partij) / ORDER_BY(pol:omschrijving);\n" +
                "\n" +
                "        graphs:\n" +
                "            live:\n" +
                "            -   hoedanigheidSoortGraph\n" +
                "            -   hoedanighedenQuery() / pol:hoedanigheidSoort / GRAPH\n" +
                "            edit:\n" +
                "            -   $instantie / GRAPH\n" +
                "\n" +
                "        payload:\n" +
                "            partijen:\n" +
                "                value: gesorteerdePersonen($instantie, $entiteitPersoonAttribuut)\n" +
                "                list: true\n" +
                "            geselecteerdeHoedanigheden:\n" +
                "                query: gesorteerdeGeselecteerdeHoedanigheden\n" +
                "                list: true\n" +
                "            nietGeselecteerdeHoedanigheidSoorten:\n" +
                "                query: gesorteerdeNietGeselecteerdeHoedanigheidSoorten\n" +
                "                list: true\n" +
                "\n" +
                "        onStart: |\n" +
                "\n" +
                "            # Als er nog geen personen zijn, dan direct door naar toevoegen\n" +
                "            IF (hoedanighedenQuery() / pol:partij[rdf:type == /pol:NatuurlijkPersoon] / COUNT == 0) {\n" +
                "                @ToevoegenPersoon!nested($instantie, $entiteitPersoonAttribuut, $partijHoedanigheidType);\n" +
                "                @COMMIT();\n" +
                "            }\n" +
                "\n" +
                "            # Skip leeg pagina na klikken op annuleren of terug knop\n" +
                "            IF (hoedanighedenQuery() / pol:partij[rdf:type == /pol:NatuurlijkPersoon] / COUNT == 0) {\n" +
                "                @DONE();\n" +
                "            }\n" +
                "\n" +
                "        actions:\n" +
                "            nieuw:\n" +
                "                onSelect: |\n" +
                "                    @ToevoegenPersoon!nested($instantie, $entiteitPersoonAttribuut, $partijHoedanigheidType);\n" +
                "                    @COMMIT();\n" +
                "            wijzigen:\n" +
                "                params:\n" +
                "                -   $partij\n" +
                "                onSelect: |\n" +
                "                    VAR $persoon, $committed = @PersoonHandmatigWijzigen!nested($partij);\n" +
                "                    IF ($committed) {\n" +
                "\n" +
                "                        $partij = $persoon;\n" +
                "                        @COMMIT();\n" +
                "                    }\n" +
                "            verwijderen:\n" +
                "                params:\n" +
                "                -   $partij\n" +
                "                onSelect: |\n" +
                "                    #\n" +
                "                    # Dit is het verwijderen van een persoon met al z'n rollen\n" +
                "                    #\n" +
                "                    VAR $_, $committed = @Bevestig!dialog('Verwijderen', 'Weet u het zeker dat u deze persoon wilt verwijderen?', 'Verwijder', 'Annuleer');\n" +
                "                    IF ($committed) {\n" +
                "                        \n" +
                "                        VAR $hoedanigheden = hoedanighedenQuery()[pol:partij == $partij];\n" +
                "                        hoedanighedenQuery() -= $hoedanigheden;\n" +
                "                        @DESTROY($hoedanigheden);\n" +
                "                        @verwijderDossierPersoon!nested($instantie / GRAPH, $partij);\n" +
                "                        @COMMIT();\n" +
                "                    }\n" +
                "            rolToevoegen:\n" +
                "\n" +
                "                params:\n" +
                "                -   $partij\n" +
                "                -   $hoedanigheidSoort\n" +
                "                onSelect: |\n" +
                "                    IF (hoedanighedenQuery()[pol:partij == $partij AND pol:hoedanigheidSoort == $hoedanigheidSoort] / NOT EXISTS){\n" +
                "                        IF (hoedanighedenQuery()[pol:partij == $partij] / pol:hoedanigheidSoort / NOT EXISTS){\n" +
                "                            #\n" +
                "                            # Gebruik bestaande hoedanigheid zonder soort\n" +
                "                            @ASSIGN(\n" +
                "                                hoedanighedenQuery()[pol:partij == $partij],\n" +
                "                                /pol:hoedanigheidSoort, $hoedanigheidSoort,\n" +
                "                                /pol:beginDatum, CURRENT_DATE()\n" +
                "                            );\n" +
                "                        }\n" +
                "                        ELSE {\n" +
                "                            #\n" +
                "                            # Maak een nieuwe hoedanigheid aan\n" +
                "                            #\n" +
                "                            VAR $hoedanigheid = @NEW($partijHoedanigheidType, $instantie / GRAPH);\n" +
                "\n" +
                "                            @ASSIGN(\n" +
                "                                $hoedanigheid,\n" +
                "                                /pol:partij, $partij,\n" +
                "                                /pol:hoedanigheidSoort, $hoedanigheidSoort,\n" +
                "                                /pol:beginDatum, CURRENT_DATE()\n" +
                "                            );\n" +
                "                            hoedanighedenQuery() += $hoedanigheid;\n" +
                "                        }\n" +
                "                    }\n" +
                "                    @COMMIT();\n" +
                "            rolVerwijderen:\n" +
                "                \n" +
                "                params:\n" +
                "                -   $partij\n" +
                "                -   $hoedanigheid\n" +
                "\n" +
                "                onSelect: |\n" +
                "                    IF ($hoedanigheid / EXISTS){\n" +
                "                        $hoedanigheid / pol:hoedanigheidSoort = null;\n" +
                "                        IF (hoedanighedenQuery()[pol:partij == $partij] / COUNT > 1) {\n" +
                "                            hoedanighedenQuery() -= $hoedanigheid;\n" +
                "                            @DESTROY($hoedanigheid);\n" +
                "                        }\n" +
                "                    }\n" +
                "                    @COMMIT();\n";
    }
}
