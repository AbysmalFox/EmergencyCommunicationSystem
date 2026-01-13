package com.example.emergencycommunicationsystem.data

import com.example.emergencycommunicationsystem.data.models.EmergencyCategory
import com.example.emergencycommunicationsystem.data.models.EmergencyGuide
import com.example.emergencycommunicationsystem.data.models.EmergencyTip
import com.example.emergencycommunicationsystem.data.models.TipPriority

/**
 * Repository of emergency guides and tips
 */
object EmergencyGuidesData {
    
    val allGuides: List<EmergencyGuide> = listOf(
        // Medical Emergencies
        EmergencyGuide(
            id = "heart_attack",
            title = "Heart Attack",
            category = EmergencyCategory.MEDICAL,
            icon = "❤️",
            description = "Immediate action required for suspected heart attack",
            tips = listOf(
                EmergencyTip(
                    "Call Emergency Services Immediately",
                    "Dial 911 or your local emergency number. Time is critical.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Help the Person Sit Down",
                    "Have them rest in a comfortable position, preferably sitting up with knees bent.",
                    TipPriority.HIGH
                ),
                EmergencyTip(
                    "Give Aspirin if Available",
                    "If the person is conscious and not allergic, give them one adult aspirin (325mg) to chew.",
                    TipPriority.HIGH
                ),
                EmergencyTip(
                    "Loosen Tight Clothing",
                    "Remove or loosen any tight clothing, especially around the neck and chest.",
                    TipPriority.NORMAL
                ),
                EmergencyTip(
                    "Stay Calm and Reassure",
                    "Keep the person calm. Anxiety can worsen the situation.",
                    TipPriority.NORMAL
                ),
                EmergencyTip(
                    "Monitor Breathing",
                    "Watch for signs of cardiac arrest. Be prepared to perform CPR if trained.",
                    TipPriority.CRITICAL
                )
            )
        ),
        EmergencyGuide(
            id = "stroke",
            title = "Stroke",
            category = EmergencyCategory.MEDICAL,
            icon = "🧠",
            description = "Recognize and respond to stroke symptoms",
            tips = listOf(
                EmergencyTip(
                    "Call Emergency Services Immediately",
                    "Time is critical. Note the time when symptoms first appeared.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Remember FAST",
                    "Face drooping, Arm weakness, Speech difficulty, Time to call emergency services.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Keep Person Comfortable",
                    "Have them lie down with head slightly elevated. Turn head to one side if vomiting.",
                    TipPriority.HIGH
                ),
                EmergencyTip(
                    "Do Not Give Food or Water",
                    "The person may have difficulty swallowing.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Note Symptoms",
                    "Observe and note all symptoms to report to medical professionals.",
                    TipPriority.HIGH
                )
            )
        ),
        EmergencyGuide(
            id = "choking",
            title = "Choking",
            category = EmergencyCategory.MEDICAL,
            icon = "😷",
            description = "Help someone who is choking",
            tips = listOf(
                EmergencyTip(
                    "Encourage Coughing",
                    "If the person can cough or speak, encourage them to keep coughing.",
                    TipPriority.HIGH
                ),
                EmergencyTip(
                    "Perform Heimlich Maneuver",
                    "Stand behind the person, place hands above the navel, and give quick upward thrusts.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Call Emergency Services",
                    "If the person becomes unconscious, call 911 immediately.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "For Pregnant Women",
                    "Place hands higher on the chest, just above the joining of the lowest ribs.",
                    TipPriority.HIGH
                ),
                EmergencyTip(
                    "For Infants",
                    "Hold face down on your forearm, support the head, and give 5 back blows between shoulder blades.",
                    TipPriority.CRITICAL
                )
            )
        ),
        
        // Natural Disasters
        EmergencyGuide(
            id = "earthquake",
            title = "Earthquake",
            category = EmergencyCategory.NATURAL_DISASTER,
            icon = "🌍",
            description = "What to do during and after an earthquake",
            tips = listOf(
                EmergencyTip(
                    "Drop, Cover, and Hold On",
                    "Drop to your hands and knees, cover your head and neck, and hold on to sturdy furniture.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Stay Away from Windows",
                    "Move away from windows, glass, and heavy objects that could fall.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "If Outdoors",
                    "Move to an open area away from buildings, trees, and power lines.",
                    TipPriority.HIGH
                ),
                EmergencyTip(
                    "If in a Vehicle",
                    "Pull over to a clear location, stop, and stay inside the vehicle.",
                    TipPriority.HIGH
                ),
                EmergencyTip(
                    "After Shaking Stops",
                    "Check for injuries and hazards. Be prepared for aftershocks.",
                    TipPriority.HIGH
                ),
                EmergencyTip(
                    "Evacuate if Necessary",
                    "If in a damaged building, evacuate carefully and go to a safe location.",
                    TipPriority.HIGH
                )
            )
        ),
        EmergencyGuide(
            id = "flood",
            title = "Flood",
            category = EmergencyCategory.NATURAL_DISASTER,
            icon = "🌊",
            description = "Stay safe during flooding",
            tips = listOf(
                EmergencyTip(
                    "Move to Higher Ground",
                    "Evacuate immediately to higher ground. Do not wait for instructions if you see rising water.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Never Walk Through Floodwater",
                    "Just 6 inches of moving water can knock you down. Water may be contaminated or hide hazards.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Do Not Drive Through Flooded Areas",
                    "Turn around, don't drown. Most flood deaths occur in vehicles.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Stay Away from Bridges",
                    "Avoid bridges over fast-moving water. They can collapse without warning.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Turn Off Electricity",
                    "If safe to do so, turn off electricity at the main breaker before water enters your home.",
                    TipPriority.HIGH
                ),
                EmergencyTip(
                    "Listen to Emergency Broadcasts",
                    "Stay tuned to local news and emergency services for updates and evacuation orders.",
                    TipPriority.HIGH
                )
            )
        ),
        EmergencyGuide(
            id = "typhoon",
            title = "Typhoon/Hurricane",
            category = EmergencyCategory.WEATHER,
            icon = "🌀",
            description = "Prepare for and survive a typhoon",
            tips = listOf(
                EmergencyTip(
                    "Evacuate if Ordered",
                    "Follow evacuation orders from authorities. Do not wait until it's too late.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Stay Indoors",
                    "If not evacuating, stay indoors away from windows and glass doors.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Secure Your Home",
                    "Board up windows, secure outdoor objects, and bring in loose items.",
                    TipPriority.HIGH
                ),
                EmergencyTip(
                    "Have Emergency Supplies",
                    "Stock up on water, non-perishable food, batteries, and first aid supplies.",
                    TipPriority.HIGH
                ),
                EmergencyTip(
                    "Stay in Interior Room",
                    "During the storm, stay in a small interior room, closet, or hallway on the lowest level.",
                    TipPriority.HIGH
                ),
                EmergencyTip(
                    "Avoid Flooded Areas",
                    "After the storm, avoid flooded areas and downed power lines.",
                    TipPriority.CRITICAL
                )
            )
        ),
        
        // Fire
        EmergencyGuide(
            id = "fire",
            title = "Fire",
            category = EmergencyCategory.FIRE,
            icon = "🔥",
            description = "What to do in case of fire",
            tips = listOf(
                EmergencyTip(
                    "Alert Others and Evacuate",
                    "Alert others immediately and evacuate the building. Do not stop to collect belongings.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Use Stairs, Never Elevators",
                    "Always use stairs during a fire. Elevators can trap you or malfunction.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Stay Low",
                    "Crawl low under smoke. The air is cleaner near the floor.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Feel Doors Before Opening",
                    "Feel closed doors with the back of your hand. If hot, use another exit.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "If Trapped",
                    "Seal the room, cover vents, and signal for help from a window.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Stop, Drop, and Roll",
                    "If your clothes catch fire, stop, drop to the ground, and roll to extinguish flames.",
                    TipPriority.CRITICAL
                )
            )
        ),
        
        // Crime
        EmergencyGuide(
            id = "robbery",
            title = "Robbery/Theft",
            category = EmergencyCategory.CRIME,
            icon = "💰",
            description = "What to do if you're a victim of robbery",
            tips = listOf(
                EmergencyTip(
                    "Stay Calm",
                    "Remain calm and do not resist. Your safety is more important than property.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Cooperate",
                    "Give the robber what they ask for. Do not make sudden movements.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Observe Details",
                    "Try to remember physical description, clothing, vehicle, and direction of escape.",
                    TipPriority.HIGH
                ),
                EmergencyTip(
                    "Call Police Immediately",
                    "Call 911 as soon as it's safe to do so. Report all details you remember.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Do Not Follow",
                    "Do not follow or chase the robber. Let police handle the situation.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Preserve Evidence",
                    "Do not touch anything the robber may have touched. Preserve the scene for police.",
                    TipPriority.HIGH
                )
            )
        ),
        EmergencyGuide(
            id = "assault",
            title = "Assault",
            category = EmergencyCategory.CRIME,
            icon = "⚠️",
            description = "What to do if you're being assaulted",
            tips = listOf(
                EmergencyTip(
                    "Get to Safety",
                    "If possible, run to a safe, public place with people around.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Call for Help",
                    "Scream for help, call 911, or use a personal safety app if available.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Fight Back if Necessary",
                    "If escape is not possible, fight back using self-defense techniques if you know them.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Seek Medical Attention",
                    "Even if injuries seem minor, seek medical attention immediately.",
                    TipPriority.HIGH
                ),
                EmergencyTip(
                    "Report to Police",
                    "Report the incident to police as soon as possible. Preserve evidence.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Seek Support",
                    "Contact support services for victims of assault. You don't have to go through this alone.",
                    TipPriority.HIGH
                )
            )
        ),
        
        // Accidents
        EmergencyGuide(
            id = "car_accident",
            title = "Car Accident",
            category = EmergencyCategory.ACCIDENT,
            icon = "🚗",
            description = "What to do after a car accident",
            tips = listOf(
                EmergencyTip(
                    "Check for Injuries",
                    "First, check yourself and passengers for injuries. Call 911 if anyone is hurt.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Move to Safety",
                    "If possible, move vehicles to the side of the road to avoid further accidents.",
                    TipPriority.HIGH
                ),
                EmergencyTip(
                    "Call Police",
                    "Call police to report the accident, especially if there are injuries or significant damage.",
                    TipPriority.HIGH
                ),
                EmergencyTip(
                    "Exchange Information",
                    "Exchange contact and insurance information with other drivers involved.",
                    TipPriority.HIGH
                ),
                EmergencyTip(
                    "Document the Scene",
                    "Take photos of the accident scene, vehicle damage, and license plates.",
                    TipPriority.HIGH
                ),
                EmergencyTip(
                    "Get Witness Information",
                    "If there are witnesses, get their contact information and statements.",
                    TipPriority.NORMAL
                )
            )
        ),
        EmergencyGuide(
            id = "burns",
            title = "Burns",
            category = EmergencyCategory.MEDICAL,
            icon = "🔥",
            description = "First aid for burns",
            tips = listOf(
                EmergencyTip(
                    "Cool the Burn",
                    "Run cool (not cold) water over the burn for at least 10 minutes or until pain subsides.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Remove Tight Items",
                    "Remove jewelry, belts, or tight clothing near the burn before swelling occurs.",
                    TipPriority.HIGH
                ),
                EmergencyTip(
                    "Cover the Burn",
                    "Cover the burn with a clean, dry cloth or sterile bandage. Do not use cotton.",
                    TipPriority.HIGH
                ),
                EmergencyTip(
                    "Do Not Break Blisters",
                    "Never break blisters. This can lead to infection.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Seek Medical Help",
                    "For severe burns, electrical burns, or burns on face/hands, seek immediate medical attention.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Do Not Apply Ice",
                    "Never apply ice, butter, or ointments to burns. This can cause further damage.",
                    TipPriority.CRITICAL
                )
            )
        ),
        
        // More Medical Emergencies
        EmergencyGuide(
            id = "seizure",
            title = "Seizure",
            category = EmergencyCategory.MEDICAL,
            icon = "⚡",
            description = "How to help someone having a seizure",
            tips = listOf(
                EmergencyTip(
                    "Stay Calm and Time the Seizure",
                    "Note the time the seizure starts. Most seizures last 1-2 minutes.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Protect from Injury",
                    "Clear the area of hard or sharp objects. Place something soft under their head.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Do Not Restrain",
                    "Never hold the person down or put anything in their mouth. This can cause injury.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Turn on Side",
                    "Gently turn the person onto their side to help keep their airway clear.",
                    TipPriority.HIGH
                ),
                EmergencyTip(
                    "Call Emergency Services",
                    "Call 911 if the seizure lasts more than 5 minutes, if another seizure follows, or if the person is injured.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Stay with Person",
                    "Stay with the person until the seizure ends and they are fully alert. Be reassuring and calm.",
                    TipPriority.HIGH
                )
            )
        ),
        EmergencyGuide(
            id = "diabetic_emergency",
            title = "Diabetic Emergency",
            category = EmergencyCategory.MEDICAL,
            icon = "🍬",
            description = "Recognize and respond to diabetic emergencies",
            tips = listOf(
                EmergencyTip(
                    "Recognize Symptoms",
                    "Low blood sugar: confusion, shaking, sweating. High blood sugar: extreme thirst, frequent urination, fruity breath.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "For Low Blood Sugar",
                    "If conscious, give 15-20g of fast-acting sugar (glucose tablets, juice, or candy). Wait 15 minutes and recheck.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "For High Blood Sugar",
                    "If person is unconscious or very ill, call 911 immediately. Do not give insulin unless trained.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Call Emergency Services",
                    "Call 911 if the person is unconscious, having seizures, or unable to swallow safely.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Monitor Closely",
                    "Stay with the person and monitor their condition. Keep them comfortable and safe.",
                    TipPriority.HIGH
                ),
                EmergencyTip(
                    "Check for Medical ID",
                    "Look for a medical alert bracelet or card that provides important information.",
                    TipPriority.HIGH
                )
            )
        ),
        EmergencyGuide(
            id = "allergic_reaction",
            title = "Severe Allergic Reaction",
            category = EmergencyCategory.MEDICAL,
            icon = "🚨",
            description = "Anaphylaxis - life-threatening allergic reaction",
            tips = listOf(
                EmergencyTip(
                    "Call Emergency Services Immediately",
                    "Anaphylaxis is a medical emergency. Call 911 right away.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Use Epinephrine if Available",
                    "If the person has an epinephrine auto-injector (EpiPen), help them use it immediately.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Help Person Lie Down",
                    "Have them lie flat with legs elevated, unless they're having trouble breathing.",
                    TipPriority.HIGH
                ),
                EmergencyTip(
                    "Remove Allergen if Possible",
                    "If you can identify and safely remove the allergen (like a bee stinger), do so.",
                    TipPriority.HIGH
                ),
                EmergencyTip(
                    "Monitor Breathing",
                    "Watch for signs of breathing difficulty. Be prepared to perform CPR if they stop breathing.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Do Not Give Food or Drink",
                    "Do not give anything by mouth if the person is having trouble breathing or swallowing.",
                    TipPriority.CRITICAL
                )
            )
        ),
        EmergencyGuide(
            id = "poisoning",
            title = "Poisoning",
            category = EmergencyCategory.MEDICAL,
            icon = "☠️",
            description = "What to do in case of poisoning",
            tips = listOf(
                EmergencyTip(
                    "Call Poison Control or 911",
                    "Call your local poison control center or 911 immediately. Have the container or substance information ready.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Do Not Induce Vomiting",
                    "Never induce vomiting unless specifically instructed by poison control or medical professionals.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Remove from Source",
                    "If safe, remove the person from the source of poison. Ventilate the area if it's a gas or fume.",
                    TipPriority.HIGH
                ),
                EmergencyTip(
                    "Save the Container",
                    "Keep the poison container or sample to show medical professionals. Note the amount ingested.",
                    TipPriority.HIGH
                ),
                EmergencyTip(
                    "Monitor Vital Signs",
                    "Check breathing, pulse, and consciousness. Be prepared to perform CPR if necessary.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Follow Medical Instructions",
                    "Follow instructions from poison control or emergency services exactly. Do not give anything unless instructed.",
                    TipPriority.CRITICAL
                )
            )
        ),
        
        // More Natural Disasters
        EmergencyGuide(
            id = "tsunami",
            title = "Tsunami",
            category = EmergencyCategory.NATURAL_DISASTER,
            icon = "🌊",
            description = "Survive a tsunami warning",
            tips = listOf(
                EmergencyTip(
                    "Evacuate Immediately",
                    "If you're near the coast and feel an earthquake or hear a tsunami warning, move to higher ground immediately.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Go Inland and Up",
                    "Move at least 2 miles inland or to an elevation of at least 100 feet above sea level.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Do Not Wait",
                    "Do not wait for official evacuation orders. If you feel a strong earthquake near the coast, evacuate immediately.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Avoid Low-Lying Areas",
                    "Stay away from beaches, harbors, and low-lying coastal areas. Tsunamis can travel far inland.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Do Not Return",
                    "Do not return to the coast until authorities declare it safe. Multiple waves may follow.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Stay Informed",
                    "Listen to emergency broadcasts and follow instructions from local authorities.",
                    TipPriority.HIGH
                )
            )
        ),
        EmergencyGuide(
            id = "volcano",
            title = "Volcanic Eruption",
            category = EmergencyCategory.NATURAL_DISASTER,
            icon = "🌋",
            description = "Stay safe during volcanic activity",
            tips = listOf(
                EmergencyTip(
                    "Evacuate if Ordered",
                    "Follow evacuation orders immediately. Do not delay or return until authorities say it's safe.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Avoid Low Areas",
                    "Move to higher ground. Avoid valleys and riverbeds where mudflows and lava flows may travel.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Protect from Ash",
                    "Wear long sleeves, long pants, and use goggles and a dust mask. Cover your nose and mouth.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Stay Indoors if Unable to Evacuate",
                    "Close all windows and doors. Seal gaps with wet towels or tape. Turn off air conditioning.",
                    TipPriority.HIGH
                ),
                EmergencyTip(
                    "Avoid Driving",
                    "Ash can damage engines and reduce visibility. Only drive if absolutely necessary for evacuation.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Listen to Authorities",
                    "Stay tuned to emergency broadcasts for updates on volcanic activity and evacuation orders.",
                    TipPriority.HIGH
                )
            )
        ),
        EmergencyGuide(
            id = "landslide",
            title = "Landslide",
            category = EmergencyCategory.NATURAL_DISASTER,
            icon = "⛰️",
            description = "What to do during a landslide",
            tips = listOf(
                EmergencyTip(
                    "Evacuate Immediately",
                    "If you're in a landslide-prone area and notice warning signs, evacuate immediately.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Move to Higher Ground",
                    "Get to high ground away from the path of the landslide. Move perpendicular to the flow.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Stay Away from Slopes",
                    "Avoid steep slopes, especially during heavy rain. Be aware of your surroundings.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "If Trapped",
                    "If trapped, curl into a tight ball and protect your head. Make noise to alert rescuers.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Watch for Debris",
                    "Be aware of falling rocks, trees, and other debris. Stay away from the landslide area.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Check for Injuries",
                    "After the landslide, check yourself and others for injuries. Help others if it's safe.",
                    TipPriority.HIGH
                )
            )
        ),
        
        // More Weather Emergencies
        EmergencyGuide(
            id = "tornado",
            title = "Tornado",
            category = EmergencyCategory.WEATHER,
            icon = "🌪️",
            description = "Stay safe during a tornado",
            tips = listOf(
                EmergencyTip(
                    "Take Shelter Immediately",
                    "Go to the lowest level of a sturdy building. Get to an interior room or hallway away from windows.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Get Under Something Sturdy",
                    "Get under a heavy table or desk. Cover your head and neck with your arms and a blanket or coat.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Avoid Windows",
                    "Stay away from windows, doors, and outside walls. Flying debris is the main danger.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "If in a Vehicle",
                    "Do not try to outrun a tornado. Get out and lie flat in a low-lying area, covering your head.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "If Outdoors",
                    "Lie flat in a low-lying area or ditch. Cover your head with your hands. Avoid overpasses and bridges.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Stay Informed",
                    "Listen to weather radio or local news for tornado warnings and updates.",
                    TipPriority.HIGH
                )
            )
        ),
        EmergencyGuide(
            id = "heat_wave",
            title = "Heat Wave",
            category = EmergencyCategory.WEATHER,
            icon = "☀️",
            description = "Protect yourself from extreme heat",
            tips = listOf(
                EmergencyTip(
                    "Stay Hydrated",
                    "Drink plenty of water, even if you don't feel thirsty. Avoid alcohol and caffeine.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Stay Indoors",
                    "Stay in air-conditioned places as much as possible. If no AC, go to public cooling centers.",
                    TipPriority.HIGH
                ),
                EmergencyTip(
                    "Avoid Peak Sun Hours",
                    "Limit outdoor activities to early morning or evening. Avoid the hottest part of the day (10am-4pm).",
                    TipPriority.HIGH
                ),
                EmergencyTip(
                    "Wear Light Clothing",
                    "Wear lightweight, light-colored, loose-fitting clothing. Use a wide-brimmed hat and sunscreen.",
                    TipPriority.HIGH
                ),
                EmergencyTip(
                    "Check on Vulnerable People",
                    "Check on elderly neighbors, young children, and those with chronic illnesses. They're at higher risk.",
                    TipPriority.HIGH
                ),
                EmergencyTip(
                    "Recognize Heat Illness",
                    "Watch for signs of heat exhaustion: heavy sweating, weakness, dizziness, nausea. Seek medical help if severe.",
                    TipPriority.CRITICAL
                )
            )
        ),
        EmergencyGuide(
            id = "blizzard",
            title = "Blizzard/Snowstorm",
            category = EmergencyCategory.WEATHER,
            icon = "❄️",
            description = "Stay safe during severe winter weather",
            tips = listOf(
                EmergencyTip(
                    "Stay Indoors",
                    "Stay indoors and avoid unnecessary travel. If you must go out, dress in layers and limit exposure.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Prepare Emergency Supplies",
                    "Have food, water, medications, flashlights, batteries, and a battery-powered radio ready.",
                    TipPriority.HIGH
                ),
                EmergencyTip(
                    "Keep Warm",
                    "Wear layers of loose-fitting, lightweight clothing. Keep your head, hands, and feet covered.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Avoid Overexertion",
                    "Shoveling snow can cause heart attacks. Take frequent breaks and don't overexert yourself.",
                    TipPriority.HIGH
                ),
                EmergencyTip(
                    "Watch for Frostbite",
                    "Watch for numbness, white or grayish-yellow skin, or firm/waxy feeling skin. Seek medical help immediately.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Stay Informed",
                    "Listen to weather updates and emergency information. Follow instructions from authorities.",
                    TipPriority.HIGH
                )
            )
        ),
        
        // More Crime Situations
        EmergencyGuide(
            id = "home_invasion",
            title = "Home Invasion",
            category = EmergencyCategory.CRIME,
            icon = "🏠",
            description = "What to do if someone breaks into your home",
            tips = listOf(
                EmergencyTip(
                    "Call 911 Immediately",
                    "Call emergency services as soon as it's safe to do so. Give your address and situation.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Find a Safe Hiding Place",
                    "If possible, lock yourself in a room with a phone. Barricade the door if you can.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Stay Quiet",
                    "Stay as quiet as possible. Turn off lights and electronics. Do not confront the intruder.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "If Confronted",
                    "Do not resist. Give them what they want. Your safety is more important than property.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Note Details",
                    "Try to remember physical description, clothing, voice, and any other identifying features.",
                    TipPriority.HIGH
                ),
                EmergencyTip(
                    "Wait for Police",
                    "Stay in your safe place until police arrive and confirm it's safe to come out.",
                    TipPriority.CRITICAL
                )
            )
        ),
        EmergencyGuide(
            id = "cyber_crime",
            title = "Cyber Crime/Identity Theft",
            category = EmergencyCategory.CRIME,
            icon = "💻",
            description = "Respond to cybercrime and identity theft",
            tips = listOf(
                EmergencyTip(
                    "Report Immediately",
                    "Report the crime to your local police and file a report with the Federal Trade Commission (FTC).",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Freeze Your Accounts",
                    "Contact your bank and credit card companies immediately to freeze or close compromised accounts.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Change All Passwords",
                    "Change passwords for all online accounts, especially email, banking, and social media.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Monitor Your Credit",
                    "Place a fraud alert on your credit reports. Monitor your credit regularly for unauthorized activity.",
                    TipPriority.HIGH
                ),
                EmergencyTip(
                    "Document Everything",
                    "Keep records of all communications, transactions, and steps taken to resolve the issue.",
                    TipPriority.HIGH
                ),
                EmergencyTip(
                    "Be Cautious",
                    "Be wary of phishing attempts. Never give personal information to unsolicited callers or emails.",
                    TipPriority.HIGH
                )
            )
        ),
        
        // More Accidents
        EmergencyGuide(
            id = "drowning",
            title = "Drowning",
            category = EmergencyCategory.ACCIDENT,
            icon = "🏊",
            description = "Rescue and first aid for drowning",
            tips = listOf(
                EmergencyTip(
                    "Call Emergency Services",
                    "Call 911 immediately. Time is critical in drowning situations.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Rescue Safely",
                    "If you must enter the water, use a flotation device. Do not put yourself in danger.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Start CPR if Needed",
                    "If the person is not breathing, start CPR immediately if you're trained. Continue until help arrives.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Keep Person Warm",
                    "Remove wet clothing and cover with dry blankets or clothing. Keep them warm to prevent hypothermia.",
                    TipPriority.HIGH
                ),
                EmergencyTip(
                    "Monitor Breathing",
                    "Even if the person seems fine, monitor them closely. Secondary drowning can occur hours later.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Do Not Drain Water",
                    "Do not try to drain water from the person's lungs. Focus on breathing and CPR.",
                    TipPriority.CRITICAL
                )
            )
        ),
        EmergencyGuide(
            id = "electrical_shock",
            title = "Electrical Shock",
            category = EmergencyCategory.ACCIDENT,
            icon = "⚡",
            description = "First aid for electrical shock",
            tips = listOf(
                EmergencyTip(
                    "Do Not Touch the Person",
                    "Do not touch the person if they're still in contact with the electrical source. You could be shocked too.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Turn Off Power",
                    "Turn off the power source at the circuit breaker, fuse box, or unplug the device if safe to do so.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Call Emergency Services",
                    "Call 911 immediately. Electrical shocks can cause internal injuries even if there are no visible burns.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Check for Breathing",
                    "Once safe, check if the person is breathing. Start CPR if they're not breathing and you're trained.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Treat for Shock",
                    "Keep the person lying down with legs elevated. Cover them with a blanket to keep warm.",
                    TipPriority.HIGH
                ),
                EmergencyTip(
                    "Do Not Remove Clothing",
                    "Do not remove burned clothing unless it's stuck to the skin. Wait for medical professionals.",
                    TipPriority.CRITICAL
                )
            )
        ),
        EmergencyGuide(
            id = "fall",
            title = "Serious Fall",
            category = EmergencyCategory.ACCIDENT,
            icon = "⬇️",
            description = "What to do after a serious fall",
            tips = listOf(
                EmergencyTip(
                    "Do Not Move the Person",
                    "Do not move the person unless they're in immediate danger. Moving could worsen spinal or other injuries.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Call Emergency Services",
                    "Call 911 immediately, especially if the person is unconscious, has severe pain, or cannot move.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Check for Injuries",
                    "Check for obvious injuries, bleeding, or signs of broken bones. Do not try to straighten limbs.",
                    TipPriority.HIGH
                ),
                EmergencyTip(
                    "Control Bleeding",
                    "If there's bleeding, apply direct pressure with a clean cloth. Do not remove objects embedded in wounds.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Keep Person Still",
                    "Keep the person still and calm. Support their head and neck if you must move them.",
                    TipPriority.CRITICAL
                ),
                EmergencyTip(
                    "Monitor Consciousness",
                    "Watch for changes in consciousness, breathing, or signs of shock. Be ready to perform CPR if needed.",
                    TipPriority.CRITICAL
                )
            )
        )
    )
    
    /**
     * Get guide by ID
     */
    fun getGuideById(id: String): EmergencyGuide? {
        return allGuides.find { it.id == id }
    }
    
    /**
     * Search guides by query
     */
    fun searchGuides(query: String): List<EmergencyGuide> {
        val lowerQuery = query.lowercase()
        return allGuides.filter {
            it.title.lowercase().contains(lowerQuery) ||
            it.description.lowercase().contains(lowerQuery) ||
            it.category.name.lowercase().contains(lowerQuery)
        }
    }
    
    /**
     * Get guides by category
     */
    fun getGuidesByCategory(category: EmergencyCategory): List<EmergencyGuide> {
        return allGuides.filter { it.category == category }
    }
}
