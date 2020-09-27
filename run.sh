#!/bin/sh

WEBINARCLI="java -jar target/webinar-cli-1.0-SNAPSHOT-jar-with-dependencies.jar"

# Print all webinars scheduled in the future
#$WEBINARCLI --print

# Export all webinars scheduled in the future to iCalendar -- https://tools.ietf.org/html/rfc5545
#$WEBINARCLI --export ZoomWebinars.ics

# Register John Doe as panelist to all webinars in the future until expiryDate, and send him an email with the webinar links
#$WEBINARCLI --firstName "John" --lastName "Doe" --email john.doe@example.com --expiryDate 2020-10-01 --register --sendMail

# Register John Doe as panelist to two webinars and send him an email with the webinar links
#$WEBINARCLI --webinarId 12345678901 --webinarId 67890112345 --firstName "John" --lastName "Doe" --email john.doe@example.com --register --sendMail

# Register John Doe as panelist to all webinars in specified time range and send him an email with the webinar links
#$WEBINARCLI --rangeStart $RANGE_START --rangeEnd $RANGE_END --firstName "John" --lastName "Doe" --email john.doe@example.com --register --sendMail

# Register all subscribers specified in the CSV file as panelist to all webinars in specified time range and write email messages in HTML files for review prior sending
#$WEBINARCLI --rangeStart $RANGE_START --rangeEnd $RANGE_END --list subscribers.csv --register --writeMail

# Send email to all subscribers specified in the CSV file for all webinars in specified time range
#$WEBINARCLI --rangeStart $RANGE_START --rangeEnd $RANGE_END --list subscribers.csv --sendMail
