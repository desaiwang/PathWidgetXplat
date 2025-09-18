//
//  StationTitle.swift
//  widgetExtension
//
//  Created by Steven Kideckel on 2024-06-19.
//  Copyright © 2024 orgName. All rights reserved.
//

import SwiftUI
import ComposeApp

struct StationTitle: View {
    let title: String
    let destinationStation: String?
    let width: CGFloat
    let maxHeight: CGFloat
    
    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            // Left side: Station name
            Text(
                WidgetDataFormatter().formatHeadSign(
                    title: title,
                    fits: {
                        let titleSpace = width - 16
                        let textWidth = measureTextWidth(
                            maxSize: CGSize(width: width, height: maxHeight),
                            text: $0,
                            font: UIFont.arimaStyleMedium(size: 14)
                        )
                        return (textWidth <= titleSpace).toKotlinBoolean()
                    }
                )
            )
            .font(Font.arimaStyleMedium(size: 14))
            .foregroundColor(.white)
            .italic()
            
            // Right side: Destination station with arrow
            if let destination = destinationStation {
                HStack(spacing: 2) {
                    Image(systemName: "arrow.right")
                        .font(.system(size: 12))
                        .foregroundColor(.white)
                    Text(destination)
                        .font(Font.arimaStyle(size: 14))
                        .italic()
                        .foregroundColor(.white)
                }
            }
        }
        .padding(.leading, 8)
    }
}
