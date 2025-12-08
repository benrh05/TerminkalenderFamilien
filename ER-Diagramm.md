```mermaid

erDiagram
    
    PERSON {
        int id PK
        string name
        string passwort_hash
        string rolle
    }

    KATEGORIE {
        int id PK
        string name
        string farbe
    }

    TERMIN {
        int id PK
        string titel
        string beschreibung
        datetime startzeit
        datetime endzeit
        int person_id FK
        int kategorie_id FK
    }

    PERSON { TERMIN : "hat"
    KATEGORIE { TERMIN : "wird_zugeordnet"
```
