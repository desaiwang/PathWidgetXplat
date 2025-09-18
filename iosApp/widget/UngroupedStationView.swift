//
//  UngroupedStationView.swift
//  widgetExtension
//
//  Created by Steven Kideckel on 2024-06-16.
//  Copyright © 2024 orgName. All rights reserved.
//

import SwiftUI
import ComposeApp

struct UngroupedStationView: EntryView {
    
    let entry: SimpleEntry
    let station: DepartureBoardData.StationData
    let width: CGFloat
    let height: CGFloat
    
    var body: some View {
        let rowCountWith6Spacing = measureRowCount(initialHeight: height, rowSpacing: 6)
        let rowCountWith4Spacing = measureRowCount(initialHeight: height, rowSpacing: 4)
        let rowCount = max(rowCountWith4Spacing, rowCountWith6Spacing)
        let rowSpacing: CGFloat = rowCountWith4Spacing > rowCountWith6Spacing ? 4 : 6
        VStack(alignment: .leading, spacing: 4) {
            StationTitle(
                title: station.displayName, 
                destinationStation: entry.configuration.destinationStation.toStation()?.displayName,
                width: width, 
                maxHeight: height
            )
                        
            let trains = station.trains
                .filter { train in !train.isPast(now: entry.date.toKotlinInstant())}
                .prefix(rowCount)
            
            if !trains.isEmpty {
                VStack(alignment: .leading, spacing: 2) {
                    // First train - prominent display
                    let firstTrain = trains.first!
                    let firstTime = formatArrivalTime(firstTrain)
                    
                    HStack(alignment: .firstTextBaseline, spacing: 0) {
                        if entry.configuration.timeDisplay == .clock {
                            // For clock time, display as single text
                            Text(firstTime)
                                .font(Font.arimaStyleBold(size: 32))
                                .foregroundColor(.white)
                        } else {
                            // For relative time, split number and "min"
                            let (numberPart, minPart) = splitTimeString(firstTime)
                            Text(numberPart)
                                .font(Font.arimaStyleBold(size: 32))
                                .foregroundColor(.white)
                            Text(minPart)
                                .font(Font.arimaStyleBold(size: 24))
                                .foregroundColor(.white)
                        }
                    }
                    
                    // Remaining trains - secondary line
                    if trains.count > 1 {
                        let remainingTrains = Array(trains.dropFirst())
                        let remainingTimes = remainingTrains.map { formatArrivalTime($0) }
                        let alsoText = entry.configuration.timeDisplay == .clock ? "also at " : "also in "
                        let timesString = formatRemainingTimes(remainingTimes: remainingTimes, alsoText: alsoText)
                        
                        Text(timesString)
                            .font(Font.arimaStyle(size: 12))
                            .foregroundColor(.white)
                            .multilineTextAlignment(.center)
                            .offset(x: 2)
                    }
                }
                .frame(maxWidth: .infinity)
            }
            
            if (trains.isEmpty) {
                HStack {
                    Spacer()
                    Text(IosResourceProvider().getNoTrainsText())
                        .font(Font.arimaStyle(size: 11))
                        .multilineTextAlignment(.center)
                    Spacer()
                }
            }
            
            Spacer()
        }
        .frame(width: width, height: height)
    }
    
    private func splitTimeString(_ timeString: String) -> (String, String) {
        // Handle cases like "5 min", "1 min", "~5 min", "now"
        if timeString == "now" {
            return ("now", "")
        }
        
        // Check if string contains "min"
        if let minRange = timeString.range(of: " min") {
            let numberPart = String(timeString[..<minRange.lowerBound])
            let minPart = String(timeString[minRange.lowerBound...])
            return (numberPart, minPart)
        }
        
        // If no "min" found, return the whole string as number part
        return (timeString, "")
    }
    
    private func formatArrivalTime(_ train: DepartureBoardData.TrainData) -> String {
        var arrivalTime: String
        if (entry.configuration.timeDisplay == .clock) {
            arrivalTime = WidgetDataFormatter().formatTime(instant: train.projectedArrival)
            if (train.isBackfilled) {
                arrivalTime = "~" + arrivalTime
            }
        } else {
            arrivalTime = WidgetDataFormatter().formatRelativeTime(
                now: entry.date.toKotlinInstant(),
                time: train.projectedArrival
            )
        }
        
        return arrivalTime
    }
    
    private func formatRemainingTimes(remainingTimes: [String], alsoText: String) -> String {
        if entry.configuration.timeDisplay == .clock {
            // For clock times, join as-is
            return alsoText + remainingTimes.joined(separator: ", ")
        } else {
            // For relative times, extract numbers and add "mins" at the end
            let timeNumbers = remainingTimes.map { time in
                // Remove " mins" suffix if present
                if time.hasSuffix(" min") {
                    return String(time.dropLast(4))
                }
                return time
            }
            return alsoText + timeNumbers.joined(separator: ", ") + " min"
        }
    }
    
    private func measureRowCount(initialHeight: CGFloat, rowSpacing: CGFloat) -> Int {
        var height = initialHeight
        let headerHeight = measureTextHeight(text: "Updated", font: UIFont.arimaStyleBold(size: 14))
        height -= headerHeight
        
        let rowHeight = measureTextHeight(text: "To", font: UIFont.arimaStyle(size: 12)) + rowSpacing
        return Int(floor(height / rowHeight))
    }
}


