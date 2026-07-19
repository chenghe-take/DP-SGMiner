import java.util.Random;

public class Distribution {
    public static int randomSeed = -1;
	/**
	 * param pro
	 * return
	 */
	static Random rd;

    static {
        if( randomSeed == -1 ){
            rd = new Random( System.currentTimeMillis() );
			System.out.println( "Random noise" );
        }else{
            rd = new Random( randomSeed );
            System.out.println( "fixed noise" );
        }
    }
	//static Random rd = new Random(2);

	public static int nextInt(int n) {
		return rd.nextInt(n);
	}

	public static int geometric(double pro, int k) {
		pro = Math.exp(pro / k);
		double _para = pro / (1 + pro);
		double randDouble = rd.nextDouble();
		int result = 0;
		double temp;
		if (randDouble < _para) {
			temp = (Math.log(randDouble * (1 + pro)) / Math.log(pro)) - 1;
			result = (int) Math.ceil(temp);
		} else if (randDouble > _para) {
			temp = -Math.log((1 - randDouble) * (1 + pro)) / Math.log(pro);
			result = (int) Math.ceil(temp);
		} else
			result = 0;
		return result;
	//	return 0;
	}

	/**
	 * param epsilon: privacy budget
	 * param sensitivity: sensitivity of the function (eg. pattern count)
	 * return
	 */
	public static double laplace(double epsilon, int sensitivity) {
		epsilon = sensitivity / epsilon;
		double _para = 0.5;

		double a = rd.nextDouble();
		double result = 0;
		double temp = 0;
		if (a < _para) {
			temp = epsilon * Math.log(2 * a);
			result = temp;
		} else if (a > _para) {
			temp = -epsilon * Math.log(2 - 2 * a);
			result = temp;
		} else
			result = 0;
		//iejr: for debug, in the normal experiment the line "return 0" should be commented
	//	return 0;
		return result;
	}

	/**
	 * Gumbel distribution: Gumbel(0, beta)
	 * Used for exponential mechanism via Gumbel-max trick.
	 * CDF: F(x) = exp(-exp(-x/beta))
	 * Inverse CDF: x = -beta * ln(-ln(u))
	 *
	 * @param beta scale parameter (beta > 0)
	 * @return a sample from Gumbel(0, beta)
	 */
	public static double gumbel(double beta) {
		double u = rd.nextDouble();
		// Avoid log(0) and log(negative)
		u = Math.max(u, Double.MIN_NORMAL);
		return -beta * Math.log(-Math.log(u));
	}

	public static int binomial(int m, double probability) {
		double rDouble = rd.nextDouble();
		int result = 0;
		double sum = 0;
		while (result <= m) {
			sum += Math.pow(probability, result)
					* Math.pow(1 - probability, m - result)
					* calculateFCT(m, result);
			if (sum <= rDouble)
				++result;
			else
				break;
		}
		// System.out.println(rDouble + ":" + sum);
		return result;
	}

	/**
	 * 
	 * param maxCardinality
	 * param i
	 * return
	 */
	public static int calculateFCT(int maxCardinality, int i) {
		if (i >= maxCardinality || i == 0)
			return 1;
		long num = 1;
		
		if( 2*i > maxCardinality ){
			
			for( int j = maxCardinality;j >= i + 1;j-- ){
				num *= j;
			}
			
			for(; maxCardinality - i > 1; i++){
				num /= (maxCardinality - i);
			}
			
		}else{
		
			for (int j = maxCardinality; j >= maxCardinality - i + 1; j--)
				num *= j;
			for (; i > 1; i--) {
				num /= i;
			}
			
		}
	
		return (int) (num);
	}

	/**
	 * param args
	 */
	
	public static void main(String[] args) {
		for( int i = 0;i < 100;i++ ){
			double sNoisy = Distribution.laplace(0.1,1000);
            System.out.println( sNoisy );
		}
	}
}
