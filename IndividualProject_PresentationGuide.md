# Individual Project Presentation Guide

## Overview

When presenting your own project, the presentations should be **5–8 minutes** long and focus on **showcasing the major features** of the project — not walking through every line of code.

---

## What to Cover

Structure your presentation around these points, spending most of your time on the live demo:

1. **Quick Intro (30–45 sec)**
   - What the app does and the tech stack (Java, Maven, JDBC/PostgreSQL, MongoDB Driver)

2. **Architecture Overview (~1 min)**
   - Briefly show your layered structure (Presentation, Service, DAO, Model, Configuration)
   - Highlight the DAO interfaces and how they let you swap between PostgreSQL and MongoDB without touching business logic

3. **Live Demo (3–4 min)** — the core of your presentation
   - Showcase the 'application flow' of your program.
   - Creating a user/entity
   - Features/functionality to expect with new data
   - Rejections/validation failures present
   - Other related features
   - Example (Banking App):
      - Register/log in a customer
      - Open an account, show balance
      - Perform a deposit, a withdrawal, and a transfer
      - Show a rejected/invalid operation (e.g., overdraft prevention, invalid login)
      - View transaction history (and filtering, if implemented)
      - If time allows, show other features

4. **Testing (30–60 sec)**
   - Run your test suite live (if applicable) or show results
   - Mention what business rules your tests cover

5. **Wrap-Up (30-60 sec)**
   - One or two things you're proud of or found challenging
   - Any optional enhancements you completed

---

## General Presentation Tips

**Screen & Display**
- Zoom in your IDE/terminal font size so text is readable from across the room (increase font size, don't just rely on your normal working setup)
- Use **dark mode with high contrast**, or light mode with large, bold text
- Close unrelated tabs, notifications, and messaging apps before sharing your screen
- Maximize your terminal/console window; avoid tiny, cluttered windows

**Delivery**
- Practice your demo path beforehand so you're not improvising queries or account numbers live
- Have sample/seed data ready in advance — don't waste demo time typing out registration forms from scratch
- If something breaks, don't panic — briefly explain what *should* happen and move on

**Time Management**
- Time yourself a few times beforehand (5–8 minutes goes by fast!)
- Prioritize the demo over narration; show, don't just tell
- Have a mental "cut list" of lower-priority items to skip if you're running long

**Technical Prep**
- Confirm your database (PostgreSQL/MongoDB) is running and connected *before* you start presenting
- Double-check your config file is set to the database you intend to demo
