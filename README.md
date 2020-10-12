
**OMT / ODT Language Plugin voor IntelliJ**

Deze plugin is nog in ontwikkel-fase. De huidige release op develop is stabiel genoeg om te gebruiken.

# Features

## Referenties
Via referenties kan een gebruiker vanuit het gebruik van een methode (command, query) of bv variabel naar de definitie ervan navigeren. Alsmede kan vanuit de definitie een 'find usage' gestart worden die aangeeft waar in het project ze gebruikt worden. Op dit moment wordt dit mechanisme ondersteund voor:

 - OMT Runnables: Activity, Procedure, StandaloneQuery
 - ODT Members: Commands, Queries
 - Variabelen
 - Prefixes

## Rename-refactoring
Refactor-rename is beschikbaar voor alle bovenstaande referenties

## OMT Model
Het OMT Model, wat gebruikt wordt om de vormgeving van alle OMT objecten te bepalen is overgenomen vanuit de beschikbare informatie in het project en overgezet in een json structuur welke wordt gebruikt om te bepalen of er geen onjuiste properties worden gebruikt.
Deze zijn onderdeel van de suggesties (auto-complete)

> Sommige items bevatten lokale variabelen ($newValue) of commands (@COMMIT). Deze zijn opgenomen in de JSON en er wordt gecontroleerd of deze op de juiste plek worden ingezet. Indien de gebruiker er overheen beweegt zal getoond worden dat ze op die plek beschikbaar zijn als local command/variabel

## ODT Command / Operators
De ODT Commands en Operators worden uitgelezen uit de index files en geannoteerd met de beschikbare informatie in de markdown files. De plugin zal zelf de locatie van de index files bepalen, indien dit niet lukt kunnen deze worden ingesteld onder Preferences | Languages & Frameworks | OMT / ODT Settings.

> Sommige commands bevatten lokale variabelen ($value bij FOREACH). Indien de gebruiker er overheen beweegt zal getoond worden dat ze op die plek beschikbaar zijn als local variabel

## Ontologie model
Op dit moment wordt er in een aparte branch (data-model) gewerkt aan het gebruiken van informatie uit het ontologie model om verdere informatie te tonen in de OMT files. Deze is al in te zetten voor experimenteel gebruik.

## Annotaties
Annotaties worden door IntelliJ gebruikt om aan te geven of er iets mis in de code (errors, warnings) of om meer informatie te tonen. Op dit moment wordt het volgende geannoteerd:

#### Import
- Bestand kan niet worden gevonden => error
- Member kan niet worden gevonden in de geimporeerde file => error

#### Operators / Commands
- Aanroepen van niet bekende member => error met import suggestie indien beschikbaar
- Verkeerd aantal argumenten => error met het verwachtte aantal

#### Prefixes
 - Prefix niet declared => error met suggestie op basis van andere toepassingen in het project
 - Prefix niet gebruikt => warning

#### Variabelen
 - Variable niet declared => error
 - Variable niet gebruikt => warning => herschrijf naar $_ om de warning weg te halen. 

## Kleurtjes
De color scheme voor de OMT / ODT elementen kan worden ingesteld bij Preferences | Editor | Color Scheme | OMT


## Installatie
Op dit moment is er nog geen officiele release versie beschikbaar. Deze kan echter eenvoudig gebouwd worden door development branch binnen te trekken en te bouwen. Eerst de flex file bouwen -> dit wordt de Lexer, daarna de bnf -> die wordt de Grammar parser. Als laatste via Gradle Task -> Build plugin. Vervolgens de zip file selecteren in de Preferences | Plugins | Install plugin from disk (het tandwieltje naast Installed) 
