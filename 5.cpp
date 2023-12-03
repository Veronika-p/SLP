#include <deque>

class net
{   std::deque<float*> w;    // весовые коэффициенты (== это образцам)
    const unsigned     n;    // количество входов сети
    
    float activation(float v) const
    {   return v>n?n:v<0?0:v;
    }
public:
    net(unsigned numInputs):n(numInputs){}
    ~net(void)
    {   for(unsigned i(0); i<w.size();) delete [] w[i++];
    }
    unsigned learnImage(const float* const image)
    {   w.push_back( (float*)memcpy( new float[n],image, sizeof(float)*n) );
        return w.size();
    }
    unsigned recognizeImage(const float* const image) const
    {   float*  axons( (float*)memset(new float[w.size()],0,sizeof(float)*w.size()) );
        float   sum1(.0), sum2(.0), e(0.95f/w.size());
        unsigned i,j;

        for(i=0; i < w.size(); ++i)    // цикл инициализации сети
        {   for(j=0; j < n; ++j)
                axons[i] += image[j]*w[i][j];
            sum1 += axons[i] = activation(0.5f*(axons[i]+float(n) ) );
        }
                
        while( sum2 != sum1 )        // цикл пока выходы сети изменяются
        {   sum1 = 0.0f * ( sum2 = sum1 );
            for(i=0; i < w.size(); ++i)
                sum1 += axons[i] = activation( axons[i]-e*(sum2-axons[i]) );
        }
        for(i=w.size(); i!=-1u && !axons[i--];);    // поиск активного аксона сети 
        delete [] axons;
        return i;
    }
/*  // аналогично recognizeImage, только, судя по алгоритму,
    // можно обойтись только измерением максимального выхода
    // если максимальных > 1, то однозначного ответа сеть не даст
    unsigned recognizeImageFast(const float* const image) const
    {   float sumMax(-1e+37f);  // максимальная сумма на выходе 
        unsigned iMax(-1);        // индекс максимального совпадения образов
        for(unsigned i=0; i < w.size(); ++i)    // цикл инициализации сети
        {   float   sum1(0.0f);
        for(unsigned j=0; j < n; ++j)
                sum1 += image[j]*w[i][j];
            sum1 = activation(0.5f*(sum1+float(n)));
            if (sum1 > sumMax)
            {    sumMax = sum1;
                iMax = i;
            }
            else if( sum1 == sumMax )
            {    iMax = -1;                
            }
        }
        return iMax;
    }
*/
};

int _tmain(int argc, _TCHAR* argv[])
{  // входы можно сделать и целочисленными, роли играть не будет
    float a1[4][15] = {{1.,1.,1.,  1.,-1.,-1.,  1.,-1.,-1.,  1.,-1.,-1.,  1.,1.,1.},// символ С
                        {1.,1.,1.,  1.,-1.,-1.,  1.,1.,-1.,   1.,-1.,-1.,  1.,1.,1.}, // Символ E
                        {1.,1.,1.,  1.,-1.,1.,   1.,-1.,1.,   1.,-1.,1.,   1.,1.,1.}, // Символ О
                        {1.,1.,1.,  1.,-1.,1.,   1.,-1.,1.,   1.,-1.,1.,   1.,1.,1.}  // искаженный
                      };

    net n(15);
    n.learnImage( a1[0] );
    n.learnImage( a1[1] );    
    n.learnImage( a1[2] );
    return n.recognizeImage( a1[3] );
}
