#include <jni.h>
#include <Computation.h>
#include <opencv2/core/core.hpp>
#include <opencv/cv.h>
#include <android/log.h>
#include <math.h>
#define LOG    "ffmpegDemo-jni" // 这个是自定义的LOG的标识
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG,__VA_ARGS__) // 定义LOGD类型
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,__VA_ARGS__) // 定义LOGI类型
#define LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG,__VA_ARGS__) // 定义LOGW类型
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG,__VA_ARGS__) // 定义LOGE类型
#define LOGF(...)  __android_log_print(ANDROID_LOG_FATAL,LOG,__VA_ARGS__) // 定义LOGF类型
using namespace cv;


const double DEGREE_120 = 2.0944;
const double PI = 3.1415926;
const double DEGREE_MAXX = 3;
enum CONDITION{
	NOTSTART = 10,
	START = 11
};
enum GESTURE{
	NOGESTURE = 100,
	WAVE = 101,
	ROLL = 102
};

IplImage iplImg;
IplImage *iplImgc;
IplImage *iplImgpyrA,*iplImgpyrB;
IplImage *iplGrayA;
IplImage *iplGrayB;
IplImage *iplGrayC;
IplImage *iplGrayD;

int level = 3;
char status[9];
float track_error[9];
CvPoint2D32f * pointsc = new CvPoint2D32f[9];
int count = 9;
int trackedNum;
int waveCount = 0;//挥手缓冲变量
long flat = 0;//清零缓冲变量
CvPoint2D32f * points = new CvPoint2D32f[9];
CvPoint2D32f * oldPoints = new CvPoint2D32f[9];
int directionAngle = 0;
int directionCount = 0;//旋转缓冲变量
bool ifFirst = true;
 double calDistance(CvPoint2D32f p1,CvPoint2D32f p2)
{
	return sqrt((p1.x-p2.x)*(p1.x-p2.x)+(p1.y-p2.y)*(p1.y-p2.y));
}

double calAngle(CvPoint2D32f p0,CvPoint2D32f p1,CvPoint2D32f p2)
{
 double a = calDistance(p0,p1);
 double b = calDistance(p0,p2);
 double c = calDistance(p1,p2);
 double cosx = (a*a+b*b-c*c)/(2.0*a*b);
 double res = acos(cosx);
 return res;
}
double calDirectionAngle(CvPoint2D32f p0,CvPoint2D32f p1)
{
	//按照直角坐标系的标准轴来计算，逆时针计算极坐标角度
	double res = 0;
	//考虑上下角度
	if(p0.x == p1.x)
	{
		if(p1.y > p0.y)
		{
			res = 3/2.0*PI;
		}
		else if(p1.y < p0.y)
		{
			res =  0.5*PI;
		}
	}
	//考虑左右角度
	if(p0.y == p1.y)
	{
		if(p1.x < p0.x)
		{
			res = PI;
		}
		else
		{
			res = 0;
		}
	}
	double y = p1.y-p0.y;
	double x = p1.x-p0.x;

	double k = y/x;
	res = atan(k);
	if(x<0 && y>0)
	{
		res += PI;
	}
	if(x<0 && y<0)
	{
		res += PI;
	}
	if(x>0 && y<0)
	{
		res += 2*PI;
	}
	return res;
}

JNIEXPORT void JNICALL Java_com_example_mycamera_Computation_Compute(JNIEnv *env, jclass, jlong matAddbf,jfloatArray point,jfloatArray pointc,jintArray conditions){
	Mat matAddaf = *(Mat*)matAddbf;
	jfloat* jpoint = env->GetFloatArrayElements(point,NULL);//用于传到java
	jfloat* jpointc = env->GetFloatArrayElements(pointc,NULL);//用于传到java
	jint* jcondition = env->GetIntArrayElements(conditions,NULL);
	if(points[0].x-jpoint[0]>1||points[0].y-jpoint[1]>1||
			jpoint[0]-points[0].x>1 || jpoint[1]-points[0].y>1)
	{
		ifFirst = true;
	}
	for(int i=0;i<9;i++)
	{
		points[i].x = jpoint[i*2];
		points[i].y = jpoint[i*2+1];
		oldPoints[i].x = pointsc[i].x;
		oldPoints[i].y = pointsc[i].y;
	}

	iplImg = matAddaf;
	if(ifFirst)
	{
		directionAngle = 0;
		iplGrayA = cvCreateImage(cvSize(320,180),IPL_DEPTH_8U,1);
		iplGrayC = cvCreateImage(cvGetSize(&iplImg),IPL_DEPTH_8U,1);
		iplGrayD = cvCreateImage(cvGetSize(&iplImg),IPL_DEPTH_8U,1);
		iplGrayB = cvCreateImage(cvSize(320,180),IPL_DEPTH_8U,1); //单通道灰度图，用于光流跟踪
		CvSize pyr_sz = cvSize(iplImg.width+8,iplImg.height/3);
		iplImgpyrA = cvCreateImage(pyr_sz,IPL_DEPTH_32F,1);
		iplImgpyrB = cvCreateImage(pyr_sz,IPL_DEPTH_32F,1);
		for(int i=0;i<9;i++)
		{
			oldPoints[i].x = points[i].x;
			oldPoints[i].y = points[i].y;
		}

	}
	cvCopy(iplGrayB,iplGrayA);
	cvCvtColor(&iplImg,iplGrayC,CV_BGR2GRAY);
	//cvSmooth(iplGrayD,iplGrayC,CV_MEDIAN,3,3);
	cvResize(iplGrayC,iplGrayB,CV_INTER_AREA);//新的Gray

	if(!ifFirst)
	{
		cvCalcOpticalFlowPyrLK(
						iplGrayA,
						iplGrayB,
						iplImgpyrA,
						iplImgpyrB,
						points,
						pointsc,
						count,
						cvSize(10,10),
						level,
						status,
						track_error,
						cvTermCriteria(CV_TERMCRIT_ITER|CV_TERMCRIT_EPS,20,0.3),
						0
				);
		//传给界面
		for(int i=0,k=0;k<9;i+=2,k++)
		{
			jpoint[i] = points[k].x;
			jpoint[i+1]=points[k].y;
			double dis = calDistance(points[k],pointsc[k]);
			if(dis<=30 && dis>1){
				jpointc[i]=pointsc[k].x;
				jpointc[i+1]=pointsc[k].y;

			}
			else
			{
				jpointc[i]=points[k].x;
				jpointc[i+1]=points[k].y;
				pointsc[k].x = points[k].x;
				pointsc[k].y = points[k].y;
			}
		}

		//进行判断

		int oppoCount = 0;
		for(int i=0;i<9;i++)
		{
			double mAngle = calAngle(points[i],pointsc[i],oldPoints[i]);

			if(mAngle > DEGREE_120)
			{
				oppoCount++;
			}
		}
		//如果满足摇手
		if(oppoCount>=7)
		{
			waveCount++;
			if(waveCount>=1)
			{
				jcondition[2]++;
				jcondition[3] = 0;
				jcondition[1] = WAVE;
				flat = 0;
				directionCount = 0;
				directionAngle = 0;
				__android_log_print(ANDROID_LOG_INFO,"gesture","wave");
				if(jcondition[2] >=5)
				{
					jcondition[0] = START;
				}
			}

		}
		else
		{
			//旋转测试

			int presentAngle = (int)(180/PI*calDirectionAngle(points[0],pointsc[0]));
			int deltaAngle = presentAngle - directionAngle;
			if(deltaAngle+360 <90) deltaAngle+=360;
			if(deltaAngle-360 >-90) deltaAngle-=360;
			__android_log_print(ANDROID_LOG_INFO,"gesture","deltaAngle:%d  , directionCount:%d",deltaAngle,directionCount);
			if(deltaAngle <100 &&deltaAngle > -100)
			{
				if(deltaAngle>0&&deltaAngle<100)
				{
					directionCount++;
				}
				if(deltaAngle<0&&deltaAngle>-100)
				{
					directionCount--;
				}
				if(deltaAngle == 0)
				{
					directionCount = 0;
				}
				//设置一个缓冲系数
				if(directionCount>5|| directionCount<-5)
				{
					jcondition[3] += deltaAngle;
					jcondition[1] = ROLL;
					jcondition[2] = 0;
					flat = 0;
					waveCount = 0;
				}
			}
			directionAngle = presentAngle;

		}

		//如果上述都处理不到
		flat++;
		if(flat >40)
		{
			jcondition[1]=NOGESTURE;
			jcondition[2]=0;
			directionAngle = 0;
			directionCount = 0;
			flat = 0;
			waveCount = 0;
		}


		//__android_log_print(ANDROID_LOG_INFO,"waveNum","%d",oppoCount);
		//__android_log_print(ANDROID_LOG_INFO,"point","y:%f",points[0].y);
	}
	ifFirst = false;
	env->ReleaseFloatArrayElements(point,jpoint,9);
	env->ReleaseFloatArrayElements(pointc,jpointc,9);
	env->ReleaseIntArrayElements(conditions,jcondition,4);
	matAddaf.release();
}


