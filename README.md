OMT / ODT language parser voor IntelliJ

Nog in ontwikkeling

Aantal punten van belang:
- Momenteel worden de OMT files in 1 lexer geanalyseerd, zowel de YAML als de OMT/ODT taal.
- Alle objecten worden als OMT objecten aangeduidt, ook indien het ODT objecten zijn.
- Het model is overgenomen uit de OPP frontend en vertaald naar een Json structuur, dit moet worden aangepast
indien er OMT-model wijzigen zijn
- De builtin commands en operators worden direct uit de files gelezen. Toevoegingen hieraan, mits in dezelfde index
worden bij een herstart van de IDE meegenomen met door plugin. Er staan geen watchers op de files, dus een herstart is nodig
- De plugin bouwt momenteel 2 indexes op: ExportMembers en Prefixes. Alle OMT files worden geanalyseerd en deze zijn
beschikbaar voor andere files om te importeren. Code-completion toont de bekende commando's.
Indien prefixes niet gedefineerd zijn zal de quickfix de bekende gebruiken van het prefix label (bv. opp:) als optionele declaraties tonen
