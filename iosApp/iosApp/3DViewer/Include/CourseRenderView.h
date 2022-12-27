//
//  CourseRenderView.h
//  iGolfViewer3D
//
//  Created by Yevhen Paschenko on 4/11/17.
//  Copyright © 2017 Yevhen Paschenko. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <GLKit/GLKit.h>
#import "OpenGLESRenderView.h"
#import "NavigationMode.h"
#import "CalloutsDrawMode.h"
#import "MeasurementSystem.h"
#import "TextureQuality.h"

@class CLLocation;
@class Vector;

@protocol CourseRenderViewDelegate <NSObject>

@optional

- (void)courseRenderViewFlyoverFinished;
- (void)courseRenderViewDidChangeNavigationMode:(NavigationMode)navigationMode;
- (void)courseRenderViewDidLoadCourseData;
- (void)courseRenderViewDidLoadHoleData;
- (void)courseRenderViewDidUpdateFlagScreenPoint:(CGPoint)point;

@end

@interface CourseRenderView: OpenGLESRenderView<UIGestureRecognizerDelegate>

@property (class, readonly) NSNotification* navigationModeDidChangeNotification;
@property (class, readonly) NSNotification* flyoverFinishedNotification;
@property (class, readonly) NSNotification* didLoadCourseDataNotification;
@property (class, readonly) NSNotification* didLoadHoleDataNotification;
@property (class, readonly) NSNotification* courseRenderViewReleasedNotification;

@property (nonatomic, assign)   NSUInteger currentHole;
@property (nonatomic, readonly) NSUInteger numberOfHoles;
@property (nonatomic, readonly) NSUInteger holeWithin;

@property (nonatomic, retain) CLLocation* currentLocation;

@property (nonatomic, assign)   BOOL showCalloutOverlay;
@property (nonatomic, assign)   BOOL showCartGpsPosition;
@property (nonatomic, assign)   BOOL autozoomActive;
@property (nonatomic, assign)   BOOL shouldSendFlagScreenPointCoordinate;
@property (nonatomic, readonly) BOOL usesOverridedPinPosition;

@property (nonatomic, readonly) CGPoint flagScreenPoint;

@property (nonatomic, assign)   MeasurementSystem measurementSystem;
@property (nonatomic, assign)   NavigationMode navigationMode;
@property (nonatomic, assign)   NavigationMode initialNavigationMode;
@property (nonatomic, assign)   CalloutsDrawMode calloutsDrawMode;

@property (nonatomic, assign)   double overallHoleViewAngle;
@property (nonatomic, assign)   double flyoverViewAngle;
@property (nonatomic, assign)   double freeCamViewAngle;

@property (nonatomic, readonly) CLLocation* flagLocation;

@property (nonatomic, weak) id <CourseRenderViewDelegate> delegate;


- (void)   loadGpsVectorData:(NSDictionary *)gpsVectorData
           andGpsDetailsData:(NSArray *)gpsDetailsData
                  andParData:(NSArray *)parData
            andElevationData:(NSDictionary *)elevationData
             andPinPositions:(NSArray *)pinPositions
           setTextureQuality:(TextureQuality)textureQuality
         andCalloutsDrawMode:(CalloutsDrawMode)calloutsDrawMode
      andShowCalloutsOverlay:(BOOL)showCalloutOverlay
        andMeasurementSystem:(MeasurementSystem)measurementSystem
    andInitialNavigationMode:(NavigationMode)navigationMode
   andCartGpsPositionVisible:(BOOL)isVisible;
- (void)viewCartWithGpsVectorData:(NSDictionary *)gpsVectorData;
- (void)setDrawingEnabled:(BOOL)isEnabled;
- (void)invalidate;
- (void)addCartMarkerWithName:(NSString*)name andLocation:(CLLocation*)location andId:(int)cartId;
- (void)updateCartMarkerWithId:(int)cartId newLocation:(CLLocation*)location;
- (void)removeCartMarkerWithId:(int)cartId;

@end
