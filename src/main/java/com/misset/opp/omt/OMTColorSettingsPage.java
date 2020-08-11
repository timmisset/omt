package com.misset.opp.omt;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.*;
import org.jetbrains.annotations.*;

import javax.swing.*;
import java.util.Map;

public class OMTColorSettingsPage implements ColorSettingsPage {
    private static final AttributesDescriptor[] DESCRIPTORS = new AttributesDescriptor[]{
            new AttributesDescriptor("Separator", OMTSyntaxHighlighter.SEPARATOR),
            new AttributesDescriptor("Global Variable Type", OMTSyntaxHighlighter.GLOBAL_VARIABLE),
            new AttributesDescriptor("Item Type", OMTSyntaxHighlighter.ITEM_TYPE),
            new AttributesDescriptor("String", OMTSyntaxHighlighter.STRING),
            new AttributesDescriptor("Number", OMTSyntaxHighlighter.NUMBER),
            new AttributesDescriptor("Boolean, Null", OMTSyntaxHighlighter.CONSTANT),
            new AttributesDescriptor("Variable", OMTSyntaxHighlighter.VARIABLE),
            new AttributesDescriptor("Bad Value", OMTSyntaxHighlighter.BAD_CHARACTER),
            new AttributesDescriptor("Comment Block", OMTSyntaxHighlighter.COMMENTBLOCK),
            new AttributesDescriptor("Operator and Command names", OMTSyntaxHighlighter.OPERATOR_OR_COMMAND),
            new AttributesDescriptor("Curie and Prefixes", OMTSyntaxHighlighter.PREFIX),


    };

    @Nullable
    @Override
    public Icon getIcon() {
        return OMTIcons.FILE;
    }

    @NotNull
    @Override
    public SyntaxHighlighter getHighlighter() {
        return new OMTSyntaxHighlighter();
    }

    @NotNull
    @Override
    public String getDemoText() {
        return "import:\n" +
                "    '@client/medewerker/src/utils/lidmaatschap.queries.omt':\n" +
                "        -   currentLidmaatschap\n" +
                "    '../utils/koppel-dossier-resource.command.omt':\n" +
                "        -   koppelDossierResource\n" +
                "    '@client/vragenlijst/src/utils/vragenlijst.queries.omt':\n" +
                "        -   verplichteVragenBeantwoord\n" +
                "    '@client/material/src/bevestig/bevestig.activity.omt':\n" +
                "        -   Bevestig\n" +
                "    '@client/persoon/src/util/kopieer-persoon.omt':\n" +
                "        -   kopieerPersoon\n" +
                "\n" +
                "prefixes:\n" +
                "    pol:         <http://ontologie.politie.nl/def/politie#>\n" +
                "    rdf:         <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "    shapesGraph: <http://ontologie.politie.nl/def/namedgraphshape/>\n" +
                "    xsd:         <http://www.w3.org/2001/XMLSchema#>\n" +
                "\n" +
                "model:\n" +
                "    Voorgeleiding: !Activity\n" +
                "        title: $titel\n" +
                "\n" +
                "        params:\n" +
                "            - $opvolgingsDossier\n" +
                "            - $voorgeleiding\n" +
                "            - $alNaarBvh  (xsd:boolean)\n" +
                "            - $alskdfj\n" +
                "    \n" +
                "        variables:\n" +
                "            -   $titel = 'Voorgeleiding'\n" +
                "            -   $bewerkbaar = true\n" +
                "            -   $voorgeleidingDossier\n" +
                "            -   $bevelAanwezig = false\n" +
                "            -   $tijdstipBevel\n" +
                "            -   $bevel\n" +
                "    \n" +
                "        queries: |\n" +
                "\n" +
                "            DEFINE QUERY bewerkbaar() => $voorgeleidingDossier / pol:dossierResource [pol:resource == currentLidmaatschap] / EXISTS AND\n" +
                "                        NOT($voorgeleidingDossier / pol:dossierStatus [pol:soort == /pol:DossierStatusSoort_Afgehandeld] / EXISTS);\n" +
                "            DEFINE QUERY rolFilter($soortCode) => pol:hoedanigheid [\n" +
                "                pol:hoedanigheidSoort / pol:hoedanigheidSoortCode == $soortCode\n" +
                "            ];\n" +
                "            DEFINE QUERY verdachtenInAanhouding() => $opvolgingsDossier / ^pol:inContextVan / pol:activiteit[rdf:type == /pol:Aanhouding] / rolFilter('Verd');\n" +
                "            DEFINE QUERY bevel($value) => CHOOSE\n" +
                "                WHEN $value == 'Ophouding voor onderzoek' => /pol:BevelSoort_OphoudingVoorOnderzoek\n" +
                "                WHEN $value == 'Invrijheidstelling' => /pol:BevelSoort_Invrijheidstelling\n" +
                "            END;\n" +
                "            DEFINE QUERY bevelReverse($iri) => CHOOSE\n" +
                "                WHEN ($iri == /pol:BevelSoort_OphoudingVoorOnderzoek) => 'Ophouding voor onderzoek'\n" +
                "                WHEN ($iri == /pol:BevelSoort_Invrijheidstelling) => 'Invrijheidstelling'\n" +
                "            END;\n" +
                "\n" +
                "        graphs:\n" +
                "            edit:\n" +
                "            -   $voorgeleiding / GRAPH\n" +
                "\n" +
                "            live:\n" +
                "            -   $voorgeleidingDossier / GRAPH\n" +
                "            -   $opvolgingsDossier / ^pol:inContextVan / GRAPH\n" +
                "            -   $opvolgingsDossier / ^pol:inContextVan / pol:activiteit / pol:hoedanigheid / pol:hoedanigheidSoort / GRAPH\n" +
                "            -   $voorgeleiding / ^pol:activiteit / pol:dossierStatus / GRAPH\n" +
                "            -   $voorgeleiding / pol:hoedanigheid / pol:hoedanigheidSoort / GRAPH\n" +
                "            -   $voorgeleiding / pol:gebeurtenisLocatieRelatie / pol:locatie / GRAPH\n" +
                "\n" +
                "        payload:\n" +
                "            alNaarBvh: $alNaarBvh\n" +
                "            locatieRelatieEntiteit: /pol:GebeurtenisLocatieRelatie\n" +
                "            bewerkbaar: $bewerkbaar\n" +
                "            voorgeleiding: $voorgeleiding\n" +
                "            voorgeleidingDossier: $voorgeleidingDossier\n" +
                "            bevelAanwezig: $bevelAanwezig\n" +
                "            bevel:\n" +
                "                value: $bevel\n" +
                "                onChange: |\n" +
                "                    IF $voorgeleiding / pol:bevelSoort / EMPTY {\n" +
                "                        $voorgeleiding / pol:tijdstipBevel = CURRENT_DATETIME;\n" +
                "                    }\n" +
                "                    $voorgeleiding / pol:bevelSoort = bevel($bevel);\n" +
                "                    $bevelAanwezig = true;\n" +
                "            huidigTijdstip: CURRENT_DATETIME()\n" +
                "\n" +
                "        returns: $voorgeleiding\n" +
                "\n" +
                "        onStart: |\n" +
                "            IF $voorgeleiding / EMPTY {\n" +
                "                $voorgeleiding = @MaakVoorgeleiding!nested($opvolgingsDossier, verdachtenInAanhouding());\n" +
                "            }\n" +
                "            IF $voorgeleiding / pol:bevelSoort / NOT EMPTY {\n" +
                "                $bevel = bevelReverse($voorgeleiding / pol:bevelSoort);\n" +
                "                $bevelAanwezig = true;\n" +
                "            }\n" +
                "\n" +
                "            $voorgeleidingDossier = $voorgeleiding / ^pol:activiteit;\n" +
                "            $bewerkbaar =  bewerkbaar();\n" +
                "\n" +
                "            $titel = $voorgeleiding / pol:activiteitSoort / pol:omschrijving;\n" +
                "\n" +
                "        onDone: |\n" +
                "            $voorgeleiding / pol:eindTijdstip = CURRENT_DATETIME;\n" +
                "\n" +
                "        rules:\n" +
                "            locatieAanwezig:\n" +
                "                $voorgeleiding / pol:gebeurtenisLocatieRelatie / pol:locatie / EXISTS            \n" +
                "            minimaalEenVerdachteRolAanwezig:\n" +
                "                $voorgeleiding / rolFilter('Verd') / EXISTS\n" +
                "            maximaalEenVerdachteRolAanwezig:\n" +
                "                $voorgeleiding / rolFilter('Verd') / COUNT < 2    \n" +
                "            vragenlijstAanwezig:\n" +
                "                $voorgeleiding / pol:verklaring / EXISTS\n" +
                "            verplichteVragenBeantwoord:  \n" +
                "                verplichteVragenBeantwoord($voorgeleiding / pol:verklaring)\n" +
                "            bevelAanwezig:\n" +
                "                $bevelAanwezig == true\n" +
                "    \n" +
                "    MaakVoorgeleiding: !Procedure\n" +
                "        params:\n" +
                "            -   $opvolgingsDossier\n" +
                "            -   $verdachtenInAanhouding\n" +
                "\n" +
                "        graphs:\n" +
                "            live:\n" +
                "                - $medewerkerGraph\n" +
                "                - $opvolgingsDossier / ^pol:inContextVan / GRAPH\n" +
                "                - $opvolgingsDossier / GRAPH\n" +
                "\n" +
                "        onRun: |\n" +
                "            VAR $voorgeleidingGraph = @NEW_GRAPH(/shapesGraph:voorgeleiding);\n" +
                "\n" +
                "            VAR $tijdstip = CURRENT_DATETIME;\n" +
                "\n" +
                "            VAR $status = @NEW(/pol:DossierStatus, $voorgeleidingGraph);\n" +
                "            $status / pol:beginTijdstip = $tijdstip;\n" +
                "            $status / pol:aangemaaktDoor = currentLidmaatschap();\n" +
                "            $status / pol:soort = /pol:DossierStatusSoort_InBehandeling;\n" +
                "\n" +
                "            VAR $voorgeleiding = @NEW(/pol:Voorgeleiding, $voorgeleidingGraph);\n" +
                "            $voorgeleiding / pol:activiteitSoort = /pol:ActiviteitSoort_Voorgeleiding;\n" +
                "            $voorgeleiding / pol:beginTijdstip = $tijdstip;\n" +
                "\n" +
                "            $voorgeleiding / pol:wijzeVanVoorgeleiding = /pol:WijzeVanVoorgeleiding_InPersoon;\n" +
                "\n" +
                "            IF($verdachtenInAanhouding / COUNT == 1) {\n" +
                "                # Kopieer de verdachte en plaats in de voorgeleiding\n" +
                "                VAR $partijHoedanigheid = @kopieerVerdachte!nested($verdachtenInAanhouding, $voorgeleidingGraph);\n" +
                "\n" +
                "                # voeg toe onder de shorthand pol:persoon en aan de container met alle PartijHoedanigheden\n" +
                "                # $voorgeleiding / pol:verdachte = $partijHoedanigheid;\n" +
                "                $voorgeleiding / pol:hoedanigheid += $partijHoedanigheid;\n" +
                "            }\n" +
                "            IF ($verdachtenInAanhouding / COUNT > 1) {\n" +
                "                #Toon een waarschuwing dat er meerdere verdachten zijn:\n" +
                "                @Bevestig!dialog('Meerdere verdachten', 'Er zijn meerdere verdachten betrokken, kan niet automatisch koppelen. Selecteer de verdachte zelf', 'Ok', '');\n" +
                "            }\n" +
                "\n" +
                "            VAR $voorgeleidingDossier = @NEW(/pol:ActiviteitDossier, $voorgeleidingGraph);\n" +
                "            $voorgeleidingDossier / pol:dossierStatus += $status;\n" +
                "            $voorgeleidingDossier / pol:wpgGrondslag = $opvolgingsDossier / pol:wpgGrondslag;\n" +
                "            $voorgeleidingDossier / pol:activiteit = $voorgeleiding;\n" +
                "            $voorgeleidingDossier / pol:inContextVan = $opvolgingsDossier;\n" +
                "\n" +
                "            @koppelDossierResource($voorgeleidingDossier, currentLidmaatschap(), null);\n" +
                "\n" +
                "            @COMMIT();\n" +
                "            RETURN $voorgeleiding;\n" +
                "\n" +
                "    kopieerVerdachte: !Procedure\n" +
                "        params:\n" +
                "            - $verdachte (pol:PartijHoedanigheid)\n" +
                "            - $graph\n" +
                "\n" +
                "        graphs:\n" +
                "            live:\n" +
                "                - $verdachte / GRAPH\n" +
                "            edit:\n" +
                "                - $graph\n" +
                "\n" +
                "        onRun: |\n" +
                "            IF($verdachte / COUNT == 1) {\n" +
                "                # Kopieer alle informatie binnen de verdachte\n" +
                "                VAR $gekopieerdeVerdachte = @COPY_IN_GRAPH($verdachte, $graph, true);\n" +
                "\n" +
                "                # De partij willen we apart kopieren, hier wijst een hoop informatie naar die nu nog niet is meegenomen omdat\n" +
                "                # de COPY_IN_GRAPH methode enkel vanuit het subject kopieert. Informatie als contact gegevens en adres valt daar dan buiten\n" +
                "                @DESTROY($gekopieerdeVerdachte / pol:partij);\n" +
                "                # gebruik generieke kopieerPersoon functie hiervoor\n" +
                "                $gekopieerdeVerdachte / pol:partij = @kopieerPersoon!nested($verdachte / pol:partij, $graph);\n" +
                "                @COMMIT();\n" +
                "                RETURN $gekopieerdeVerdachte;\n" +
                "            }\n";
    }

    @Nullable
    @Override
    public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return null;
    }

    @NotNull
    @Override
    public AttributesDescriptor[] getAttributeDescriptors() {
        return DESCRIPTORS;
    }

    @NotNull
    @Override
    public ColorDescriptor[] getColorDescriptors() {
        return ColorDescriptor.EMPTY_ARRAY;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "OMT";
    }
}
