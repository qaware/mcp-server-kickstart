# Objective
This QALab is developing an agent that makes it possible to enter timesheets for a workday via chat or speech. As a showcase for the Agentic Layer and, of course, to optimize our working environment, we are building an AI agent that makes booking faster and more convenient.

# Requirements

- The person making the booking is recognized and authenticated by Chronos using Google API.
- The agent can understand natural language text and extract the following information from it:
  - Start and end times of work
  - Break length
  - Work content and associated duration
  - The day currently being discussed (“today,” “the day before yesterday,” “August 12,” etc.)
- All projects and accounts available to the person are loaded from Chronos.
- The work content is mapped to suitable projects and accounts.
- The general data and the hours recorded are stored in Chronos.
- The interaction between human and agent takes place in spoken form.
- Open: Via which medium? Telephone, website (Chronos or own), app, ...?
- The agent learns from previous bookings to improve and individualize the assignment for the person.
- Summary of the planned use case:
- Hour recording for the current day (or the last day not recorded, if applicable)
- Recording in the first step via chat, later via voice input if necessary
- NO approval at the end of the month – this should always be done via the Chronos UI
- No deletion of entries
