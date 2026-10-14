# Stock Trading App

Android aplikacija za praćenje dioničkog portfolia, razvijena kao projekt za kolegij **Programiranje mobilnih aplikacija** na Veleučilištu u Bjelovaru.

---

## Cilj aplikacije

Aplikacija omogućuje korisniku praćenje vlastitog dioničkog portfolia u stilu Trading212 — dodavanje dionica, bilježenje kupnji i prodaja, pregled ukupne vrijednosti portfolia te povijest svih transakcija.

---

## Funkcionalnosti

### Obavezne
- **Pregled portfolia** — ukupna vrijednost, postotna promjena i sparkline graf
- **Detalji dionice** — kupnja i prodaja s automatskom rekalkulacijom prosječne cijene
- **Povijest transakcija** — filtriranje po tipu (kupnja/prodaja) i brisanje transakcija
- **Postavke** — odabir jezika (HR/EN) s perzistencijom u SharedPreferences
- **Internacionalizacija** — podrška za hrvatski i engleski jezik
- **Portrait i Landscape** — adaptirani layouti za oba načina rada

### Izborne
- **Lokalna pohrana** — Room baza podataka + SharedPreferences
- **Stilovi i teme** — konzistentni dark theme dizajn kroz cijelu aplikaciju

---

## Arhitektura

Aplikacija koristi **Single Activity Architecture** s Jetpack Composeom i MVVM uzorkom:

```
UI (Jetpack Compose)
    ↓
ViewModel
    ↓
Repository
    ↓
Room Database (SQLite)
```

---

## Baza podataka

Dvije tablice u međusobnom odnosu:

**`stock_holdings`** — dionice u portfoliu
| Stupac | Tip | Opis |
|---|---|---|
| id | Int | Primary key |
| ticker | String | Jedinstvena kratica (npr. AAPL) |
| companyName | String | Naziv kompanije |
| shares | Double | Broj dionica |
| avgBuyPrice | Double | Prosječna cijena kupnje |
| currentPrice | Double | Trenutna tržišna cijena |

**`transactions`** — svaka kupnja ili prodaja
| Stupac | Tip | Opis |
|---|---|---|
| id | Int | Primary key |
| stockTicker | String | FK → stock_holdings.ticker |
| type | TransactionType | BUY ili SELL |
| shares | Double | Broj dionica |
| pricePerShare | Double | Cijena po dionici |
| timestamp | Long | Datum i vrijeme |
| note | String | Opcionalna bilješka |

---

## Ekrani

| Ekran | Opis |
|---|---|
| **HomeScreen** | Portfolio pregled s ukupnom vrijednošću i listom dionica |
| **StockDetailScreen** | Detalji dionice, kupnja/prodaja, povijest transakcija |
| **AddStockScreen** | Forma za dodavanje nove dionice u portfolio |
| **HistoryScreen** | Sve transakcije s filter chipovima |
| **SettingsScreen** | Odabir jezika |

---

## Tehnologije

- **Kotlin** — programski jezik
- **Jetpack Compose** — deklarativni UI framework
- **Room** — lokalna baza podataka
- **Navigation Compose** — navigacija između ekrana
- **ViewModel + StateFlow** — upravljanje stanjem
- **Coroutines + Flow** — asinkrono programiranje
- **SharedPreferences** — pohrana korisničkih postavki
- **Material Design 3** — UI komponente

---

## Pokretanje

1. Kloniraj repozitorij
```bash
git clone https://gitlab.vub.hr/<username>/stock-trading-app.git
```

2. Otvori projekt u **Android Studiju**

3. Pokreni na emulatoru ili fizičkom uređaju (minimalni API: 26)

---

## Struktura projekta

```
app/src/main/java/com/example/projekt/
├── data/
│   ├── model/          # StockHolding, Transaction, StockWithTransactions
│   ├── db/             # StockDatabase, StockHoldingDao, TransactionDao
│   └── repository/     # StockRepository
├── ui/
│   ├── navigation/     # AppNavigation, BottomNavBar, Screen
│   ├── screens/        # Svi ekrani
│   ├── theme/          # Boje, tipografija, tema
│   ├── util/           # isLandscape() helper
│   └── viewmodel/      # PortfolioViewModel
└── MainActivity.kt
```

---

## Student

**Veleučilište u Bjelovaru**  
Kolegij: Programiranje mobilnih aplikacija  
Akademska godina: 2024./2025.
