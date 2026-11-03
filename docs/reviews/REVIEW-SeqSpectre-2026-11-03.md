# Intensieve review van SeqSpectre

Datum: 3 november 2026. Repository: Bio-informatics-student/Java_groep_4. Beoordeeld: main, commit 43411643469382c2f12ea1b3b873099d99d5c5f3.

De applicatie bouwt, de voorbeeldanalyse is reproduceerbaar en de onderzochte QC-formules geven correcte uitkomsten voor de ondersteunde, goed gevormde invoer. De belangrijkste problemen zitten in bestandsveiligheid, invoervalidatie en hergebruik van componenten. Er zijn concrete scenario's waarin brondata verdwijnen of ongeldige data toch PASS krijgen.

Deze review is uitgevoerd met de interne code-review-skill en de reproductieaanpak uit diagnosing-bugs. Broncode, Gradle-configuratie, tests, README, gekoppelde documentatie en UML zijn bekeken. Tijdens de beoordeling zijn geen broncode of bestaande documentatie aangepast. De tijdelijke JDK, aanvullende scripts en synthetische testdata zijn niet opgenomen in deze reviewbranch.

## Onderzoek en bewijs

| Controle | Uitkomst |
|---|---|
| Gradle test en shadowJar met Temurin 26.0.2.1+1 en Gradle 9.7.1 | BUILD SUCCESSFUL |
| Bestaande JUnit-suite | 63 ontdekt, 62 geslaagd, 1 overgeslagen, 0 failures |
| Uitgeschakelde controllertest afzonderlijk geactiveerd, zonder bronwijziging | 1 uitgevoerd, 1 gefaald door lege FASTQ-fixture |
| CLI via werkelijk gebouwde JAR | 54 verschillende scenario's, inclusief bewuste reproducties van gebreken |
| QC-referentieberekeningen | 100 reproduceerbare datasets; 892 controles zonder afwijking; seed 20261103 |
| Aanvullende API-diagnostiek | 9 controles die state- en grensgevalproblemen bevestigen |
| Aanvullende compilercontrole | javac -Xlint:all slaagt met 1 serialVersionUID-waarschuwing |
| Gradle-wrapperintegriteit | SHA-256 gelijk aan officiële Gradle-checksum: 7a9ce74cff467ca1bf60a4fcd9f05185acceda4d0f382434d393e17864262c5d |

De aanvullende controles bevatten zowel normale verwachtingen als controles die juist de aanwezigheid van een gebrek bevestigen. Een geslaagde reproductie betekent dus dat het beschreven gebrek optreedt. Er is geen statement dat alle productscenario's foutloos zijn.

De JDK is tijdelijk gedownload via de officiële Adoptium-API en met de gepubliceerde SHA-256 gecontroleerd. Er is geen systeembrede Java-installatie of blijvende PATH-wijziging gedaan. De eerdere blokkade door ontbrekende Java is hiermee voor deze review opgelost.

## 1. Hoge prioriteit: uitvoer kan invoer vernietigen

**Classificatie: bevestigd gebrek, P1.**

Main controleert of de uitvoer een directory is, maar controleert niet of deze naar een invoerbestand verwijst. OutputWriter gebruikt Files.writeString, waarmee een bestaand bestand wordt afgekapt en vervangen.

Drie varianten zijn daadwerkelijk uitgevoerd op wegwerpdata:

1. Eén bestand als invoer én uitvoer: de FASTQ-inhoud wordt vervangen door TSV.
2. Een directory als invoer, met een van de FASTQ-bestanden daarbinnen als uitvoer: dat sample wordt vervangen.
3. Een andere uitvoernaam die via een hard link hetzelfde bestand is: ook dan verdwijnt de originele FASTQ-inhoud.

Alle drie eindigen met exitcode 0 en een succesmelding. De applicatie heeft dus geen bescherming tegen onbedoeld verlies van brondata.

Bron: [Main.java:133](../../src/main/java/Main.java#L133), [OutputWriter.java:123](../../src/main/java/output/OutputWriter.java#L123).

**Hersteladvies:** ontdek alle invoerbestanden en controleer de uitvoer tegen ieder bestand voordat er wordt geschreven. Gebruik bij bestaande bestanden Files.isSameFile zodat ook hard links en padaliassen worden herkend. Vergelijk daarnaast genormaliseerde/resolved paden waar passend. Schrijf een voltooid rapport via een tijdelijk bestand en een vervangingstap.

**Acceptatie:** directe gelijkheid, directory-invoer en hard-link-alias worden geweigerd met een niet-nul exitcode; de oorspronkelijke bytes blijven gelijk.

## 2. Ongeldige sequenties krijgen PASS

**Classificatie: bevestigd gebrek in nucleotide-QC, P2.**

FastQReader controleert header, separator en gelijke lengte van sequentie en kwaliteit. De gebruikte analyse controleert de kwaliteitstekens, maar geen nucleotidealfabet. De afzonderlijke DataHandler wordt niet vanuit de actieve pipeline aangeroepen.

Deze invoer is werkelijk geanalyseerd:

    @read
    A C?
    +
    IIII

Uitkomst: Beginning Quality 100%, Ending Quality 100%, Passing Reads 100%, GC 25%, Final Status PASS, exitcode 0. Ook de sequentie 1234 wordt geaccepteerd en krijgt PASS.

Dit toont dat ongeldige of verkeerd aangeleverde sequentiedata een geloofwaardig QC-rapport kunnen opleveren. Witruimte in de sequentie is volgens de [primaire FASTQ-beschrijving](https://pmc.ncbi.nlm.nih.gov/articles/PMC2847217/) niet toegestaan.

Bron: [FastQReader.java:56](../../src/main/java/input/FastQReader.java#L56), [AnalyseReadQuality.java:98](../../src/main/java/processing/AnalyseReadQuality.java#L98).

**Hersteladvies:** kies een expliciet nucleotidealfabet en valideer het in de werkelijk gebruikte leesroute. Houd rekening met N en eventueel andere IUPAC-codes; N is een gebruikelijke ambiguïteitscode, zie de [NCBI-beschrijving van nucleotidecodes](https://www.ncbi.nlm.nih.gov/sra/docs/sra-data-submission-standards/). Bepaal ook het beleid voor hoofdletters en kleine letters.

DataHandler simpelweg aansluiten is geen complete reparatie: die accepteert alleen A/C/G/T, weigert N en controleert de ASCII-kwaliteit niet. Dit is in de API-proeven bevestigd. Consolidatie van de validatie voorkomt twee verschillende definities van een geldige read.

Een header met alleen @ wordt ook geaccepteerd. Dat verdient een expliciete keuze voor een minimaal bruikbare readidentifier.

## 3. De invoercontroller bewaart bestanden uit eerdere analyses

**Classificatie: bevestigd gebrek bij API-hergebruik, P2. De huidige CLI maakt per uitvoering een nieuwe controller.**

InputHandler bewaart ontdekte paden in velden. scanFolder voegt daar steeds aan toe. InputController bewaart dezelfde InputHandler gedurende zijn levensduur.

Reproductie met dezelfde controller:

- Eerst map A met a.fastq: resultaat bevat a.fastq.
- Daarna map B met b.fastq: resultaat bevat a.fastq én b.fastq.
- Verwijder vervolgens het eigen testbestand a.fastq en analyseer opnieuw de geldige map B: de analyse faalt op het oude, inmiddels ontbrekende bestand uit map A.

Dit kan verkeerde samples, dubbele rapportregels of onverklaarbare fouten geven zodra een UI, service of ander programma de controller hergebruikt.

Bron: [InputHandler.java:19](../../src/main/java/input/InputHandler.java#L19), [InputHandler.java:70](../../src/main/java/input/InputHandler.java#L70), [InputController.java:58](../../src/main/java/input/InputController.java#L58).

**Hersteladvies:** laat een scan een nieuw resultaat teruggeven, met lokaal opgebouwde lijsten. Daarmee wordt ook voorkomen dat een halverwege mislukte scan oude en nieuwe toestand mengt. Een test moet twee verschillende mappen achtereen op dezelfde instantie analyseren.

## 4. De groene testuitkomst geeft onvoldoende dekking van de keten

**Classificatie: bevestigd testgat, P2.**

De suite ontdekt 63 tests. Daarvan slagen er 62; de enige InputControllerTest is met @Disabled uitgezet. Er zijn geen eigen testklassen voor Main of FastqQCProcessor.

De controllertest maakt lege FASTQ-, SAM- en BAM-bestanden en verwacht vervolgens dat de analyse niet faalt. Met de conditie extern opgeheven faalt die test terecht op de lege FASTQ. Dit is een verouderde fixture/verwachting, geen nieuw geïntroduceerde productfout.

Twaalf geslaagde tests toetsen DataHandler, terwijl deze validatieroute niet in de CLI-pipeline zit. FastQReader heeft slechts één bestaande test, voor twee geldige ongecomprimeerde records. De repositorysuite dekt dus niet de foutscenario's die deze review via de echte JAR aantoonde.

Bron: [InputControllerTest.java:13](../../src/test/java/input/InputControllerTest.java#L13), [FastQReaderTest.java:21](../../src/test/java/input/FastQReaderTest.java#L21).

**Hersteladvies:** herstel de controllerfixture met echte geldige reads. Voeg ketentests toe voor gegevensbehoud, ongeldige sequenties, gzip, gemengde batches en CLI-exitcodes. Controleer uitkomsten en oorspronkelijke bestandsinhoud, niet alleen dat er geen exception optreedt.

## 5. Hoofdletters beïnvloeden duplicaatdetectie

**Classificatie: bevestigd gedrag; gewenste semantiek moet expliciet worden gekozen.**

ACGT en acgt worden beide geaccepteerd. Samen geven ze 0% duplicaten en PASS met een duplicaatlimiet van 0%. Twee keer ACGT geeft bij dezelfde limiet 50% duplicaten en FAIL. De GC-berekening behandelt de letters wel zonder onderscheid.

Bron: [DuplicateCalculator.java:45](../../src/main/java/processing/DuplicateCalculator.java#L45), [GCCalculator.java:25](../../src/main/java/processing/GCCalculator.java#L25).

De README noemt exacte matches. Letterlijke stringgelijkheid past daarbij, maar gebruikers kunnen dit opvatten als gelijke nucleotidesequenties. Kleine of gemengde letters komen volgens de [FASTQ-beschrijving](https://pmc.ncbi.nlm.nih.gov/articles/PMC2847217/) voor.

**Advies:** normaliseer sequenties als biologische gelijkheid bedoeld wordt, of weiger/documenteer de andere schrijfwijzen. Normaliseer nooit de kwaliteitstring: daarin veranderen hoofdletters en kleine letters de Phred-score.

## 6. Geheugenverbruik begrenst de bruikbaarheid

**Classificatie: bevestigde operationele beperking, al globaal genoemd in de README.**

De reader houdt alle records in het geheugen. De processor bouwt aanvullende lijsten en duplicaatdetectie gebruikt een HashSet.

Gecontroleerde belasting: 120.000 reads van 150 basen, ongecomprimeerd 37.808.890 bytes, ongeveer 36,1 MiB.

| JVM-heap | Waargenomen uitkomst |
|---|---|
| 32 MiB | OutOfMemoryError, exit 1, stacktrace |
| 64 MiB | Eerste proef voltooid; bij herhaling OutOfMemoryError |
| 256 MiB | Voltooid, PASS |
| 32 MiB, dezelfde data gzip-gecomprimeerd tot 397.137 bytes | OutOfMemoryError |

Dit is een proef met expliciete heaplimieten, geen claim dat de standaard-JVM op deze machine al bij 36 MiB invoer faalt. De grens is geen stabiele capaciteitsgarantie; GC en tijdelijke allocaties spelen mee. Gecomprimeerde bestandsgrootte zegt bovendien weinig over het benodigde heapgeheugen.

Bron: [FastQReader.java:24](../../src/main/java/input/FastQReader.java#L24), [FastqQCProcessor.java:123](../../src/main/java/processing/FastqQCProcessor.java#L123). Dit is waargenomen met synthetische FASTQ-belasting. De tijdelijke fixtures en logs zijn niet opgenomen in de repository; zie de reproductiegrenzen onderaan.

**Advies:** verwerk kwaliteits-, GC- en lengtestatistieken per record. Exacte duplicaatdetectie blijft geheugen afhankelijk van het aantal unieke sequenties vragen; daarvoor is een expliciete capaciteit/strategie nodig. Geef bij overschrijding een bruikbare foutmelding en leg geteste datasetgroottes vast.

## 7. Eén slecht batchbestand voorkomt alle nieuwe uitvoer

**Classificatie: bevestigd faalgedrag; beleid is niet duidelijk beschreven.**

Een map met één goed bestand en één lege FASTQ eindigt met exitcode 1 zonder een nieuw TSV-rapport. Eerder verzamelde resultaten worden niet opgeslagen. Als er al een rapport op de gekozen uitvoerlocatie staat, blijft dat oude rapport bestaan.

Bron: [InputController.java:74](../../src/main/java/input/InputController.java#L74), [Main.java:213](../../src/main/java/Main.java#L213).

Dit is verdedigbaar als een batch bewust volledig moet slagen. Het wordt kwetsbaar wanneer een gebruiker of vervolgstap alleen naar de aanwezigheid van results.tsv kijkt. Dan kan een oud rapport voor een nieuw resultaat worden aangezien.

**Advies:** beschrijf het batchbeleid, gebruik herkenbare uitvoer per run en laat vervolgstappen de exitcode controleren. Als doorgaan na een fout gewenst is, rapporteer expliciet een foutstatus per sample; sla fouten niet stilzwijgend over.

## 8. Een aantal fouten is moeilijk te lokaliseren

**Classificatie: bevestigde beperkingen van foutmeldingen, P3.**

- Een afgekapt gzip-bestand wordt geweigerd, maar de melding eindigt op . null. De onderliggende EOFException heeft geen bericht.
- Een niet-beschrijfbaar uitvoerbestand wordt gemeld als Could not process input, met verwijzingen naar zowel invoer als uitvoer; de uitvoerfase en oorzaak zijn onvoldoende duidelijk.
- Structurele FASTQ-fouten noemen het bestand, maar niet het recordnummer of de regel.
- OutOfMemoryError ontsnapt als stacktrace aan de gewone foutafhandeling.

Bron: [Main.java:254](../../src/main/java/Main.java#L254), [FastQReader.java:38](../../src/main/java/input/FastQReader.java#L38).

**Advies:** onderscheid lezen, analyseren en schrijven in foutcontext. Voeg bij leesfouten recordnummer en indien beschikbaar readidentifier toe. Vertaal bekende gzip- en geheugenproblemen naar herkenbare meldingen.

## 9. PASS heeft een beperkte betekenis

Deze gedragingen zijn bevestigd en passen bij de huidige instellingen:

- Een read waarvan 80% van de basen in het midden Q0 is, kan PASS krijgen wanneer beide uiteinden goed zijn. De standaardanalyse bekijkt 10% per uiteinde.
- De standaard duplicaatlimiet van 100% laat de duplicaatvoorwaarde altijd slagen.
- Na lengtefiltering wordt Passing Reads berekend over de overgebleven reads. In een proef werd de slechte, korte helft verwijderd en kreeg de resterende helft 100% Passing Reads en PASS.
- Een succesvol uitgevoerde analyse met QC-status FAIL geeft proces-exitcode 0.
- De TSV bevat geen volledige instellingen, geen totale readtelling en geen releaseversie. Een los rapport is daardoor beperkt reproduceerbaar.

Dit zijn geen bewezen rekenfouten. De README benoemt read-end-analyse en de commandoreferentie beschrijft veel van deze keuzes. Voor veilig gebruik verdient het aanbeveling deze betekenis van PASS en de exitcodes ook in de korte gebruikersroute uit te leggen en instellingen/aantallen bij een rapport te bewaren.

## 10. Onderhoud en interne API's

De volgende punten zijn direct onderzocht of uit de code afgeleid:

- De aanvullende API-proef bevestigt dat GC en duplicaten bij lege invoer NaN teruggeven en minimumlengte Integer.MAX_VALUE. De huidige CLI voorkomt deze lege datasets. Hergebruik van de helpers vraagt een duidelijk contract.
- FastqQCProcessor accepteert rechtstreeks minLength=-1; Main weigert die waarde. De grenzen verschillen dus per ingang.
- DataHandler met skipInvalidReads=true kan nog steeds een NullPointerException geven op een null-record.
- De actieve processor roept analyse aan, maar gebruikt uit het uitgebreide resultaat alleen basesPerEnd. Gemiddelden, medianen, sorteringen en foutkansen worden berekend en vervolgens niet gebruikt voor het rapport. Dit is onnodig werk op de actieve route; het is niet als afzonderlijk prestatieknelpunt gebenchmarkt.
- Reads reizen als String[] met vaste indexposities. Een klein expliciet readtype zou validatie en later hergebruik eenvoudiger maken.
- De scanner geeft zijn veranderbare interne lijsten direct terug en sorteert resultaten niet expliciet. Het eerste vergroot de mogelijkheid van externe toestandwijzigingen; het tweede geeft geen eigen garantie voor stabiele rapportvolgorde.
- De compiler meldt alleen de ontbrekende serialVersionUID op InvalidReadException. Dat heeft hier lage prioriteit.

Deze punten rechtvaardigen gerichte verbeteringen. Een brede herbouw van het project is op basis van deze review niet nodig.

## 11. README, build en architectuurdocumentatie

De kern van de README klopt met de huidige uitvoering:

| Claim | Controle |
|---|---|
| De vier FASTQ-extensies, inclusief gzip | Alle vier daadwerkelijk verwerkt |
| Een map met meerdere FASTQ-bestanden | Twee directe samples verwerkt; nested sample overgeslagen |
| Voorbeeld met vier reads van lengte 16 | Exacte voorbeeldwaarden gereproduceerd, inclusief GC 43,75% en PASS |
| JAR-pad build/libs/seqspectre-all.jar | Door shadowJar gegenereerd en gebruikt |
| Gradle en hoofddependencies | Wrapper 9.7.1, Shadow 9.6.1, Picocli 4.7.7, JUnit 5.11.4 komen overeen |
| Configureerbare scores en drempels | CLI-grenzen, decimale end-percent en inclusieve Phred-drempels gecontroleerd |
| Lengtefiltering | Gedrag overeenkomstig commandoreferentie, inclusief oorspronkelijke min/max-lengte |
| SAM/BAM/FastQC nog niet geanalyseerd | Readerklassen zijn placeholders; actieve route verwerkt FASTQ |

Resterende documentatie- en buildpunten:

1. Het UML is inhoudelijk verouderd. Het mist GC- en lengtecomponenten en toont oude signatures van InputController, QCResult en OutputWriter. Zie [SeqSpectre_UML.puml:44](../../SeqSpectre_UML.puml#L44) en de huidige bron.
2. gradlew staat in Git als mode 100644. De directe macOS/Linux-quickstart met ./gradlew kan na een normale Unix-checkout op Permission denied stoppen. De installatiegids noemt chmod +x wel. Dit is statisch vastgesteld; macOS/Linux zijn niet uitgevoerd.
3. De build legt geen Java-toolchain of release-doel vast. De hier met JDK 26 gebouwde Main heeft classfile-major 70 en verlangt dus een geschikte Java 26-runtime. De README claimt terecht geen geteste minimumversie, maar reproduceerbare builds zouden een expliciete Java-keuze moeten vastleggen.
4. De CLI noemt zichzelf qc-tool; de documentatie noemt het product SeqSpectre. --version geeft exit 0 zonder release-informatie, zoals de commandoreferentie ook vermeldt.
5. De projectboom noemt seqspectre-logo.jpg, terwijl het bestand .jpg.jpg heet. De echte afbeelding bovenaan de README verwijst wel naar het bestaande bestand.
6. File Type en Minimum Sequence Length zijn vrije benamingen in de README-tabel; de TSV-koppen zijn Type en Minimum Length. De voorbeeldwaarden zijn inhoudelijk gelijk. Dit is cosmetisch.

## Aanbevolen volgorde

1. Bescherm invoerbestanden tegen alle uitvoerpadaliassen.
2. Zorg voor één consistente, actieve readvalidatie met een afgesproken nucleotide- en lettercasebeleid.
3. Maak mapscans onafhankelijk en voeg een test voor controllerhergebruik toe.
4. Herstel de uitgeschakelde controllertest en voeg CLI-/processorketentests voor de gevonden problemen toe.
5. Leg batchbeleid, proces-exitcodes en de betekenis van PASS expliciet vast.
6. Verbeter capaciteit, foutcontext en rapportmetadata.
7. Werk vervolgens UML en kleine documentatieverschillen bij.

## Herhalen en grenzen

De reguliere build en JUnit-suite zijn uitgevoerd op de genoemde commit met Temurin 26.0.2.1+1 en Gradle 9.7.1. De build/teststap kan worden herhaald met gradlew.bat --no-daemon --console=plain test shadowJar op Windows of bash ./gradlew --no-daemon --console=plain test shadowJar op macOS/Linux.

De aanvullende CLI-, API- en geheugencontroles gebruikten tijdelijke scripts en synthetische fixtures die niet bij deze reviewbranch zijn gevoegd. De aantallen en uitkomsten hierboven zijn dus vastgelegde reviewwaarnemingen; ze zijn niet allemaal met één repositorycommando opnieuw te produceren. De seed voor de 100 referentiedatasets is 20261103.

Niet uitgevoerd: macOS/Linux-runtimecontroles, andere Java-versies, echte biologische datasets, onafhankelijke vergelijking met een andere QC-tool, langdurige productielast, gesimuleerde volle schijf of procesuitval tijdens het schrijven, of een volledige kwetsbaarhedenscan van alle dependencies. Er is geen formele line-coverage gemeten. De 100 referentiedatasets toetsen de beschreven rekenregels; zij valideren niet of die regels voor ieder biologisch onderzoeksdoel geschikt zijn.

De bevindingen gelden voor main op commit 43411643469382c2f12ea1b3b873099d99d5c5f3. Deze reviewbranch voegt alleen dit rapport toe; de beoordeling bevat geen broncodewijzigingen.
